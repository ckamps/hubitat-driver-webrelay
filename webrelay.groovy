metadata {
    definition (name: 'WebRelay Switch',
                namespace: 'ckamps', 
                author: 'Christopher Kampmeier',
                importUrl: 'https://raw.githubusercontent.com/ckamps/hubitat-drivers-webrelay/master/webrelay.groovy') {
        
        capability 'Actuator'
        capability 'Switch'
        capability 'Refresh'
    }
    preferences {
        input name: 'address',   type: 'text', title: 'WebRelay Address',     description: 'FQDN or IP address', required: true
        input name: 'logEnable', type: 'bool', title: 'Enable debug logging', defaultValue: true
    }
}

def logsOff() {
    log.warn "debug logging disabled..."
    device.updateSetting("logEnable", [value: "false", type: "bool"])
}

def updated() {
    log.info "updated..."
    log.warn "debug logging is: ${logEnable == true}"
    if (logEnable) runIn(1800, logsOff)
}

def parse(String description) {
    if (logEnable) log.debug(description)
}

def refresh() {
    if (logEnable) log.debug("attempting refresh of relay state via WebRelay: ${address}")
    def uri = "http://${address}"
    def path = '/stateFull.xml'

    response = doHttpGet(uri, path)

    if (!response) throw new Exception("doHttpGet to get current state returned empty response")
    
    if (!response.relaystate) throw new Exception("Can't find relaystate element in response from WebRelay")

    if (!response.relaystate.@value) throw new Exception("Empty value in relaystate element in response from WebRelay")

    sendEvent(
        name: 'switch',
        value: (response.relaystate == '0') ? 'off' : 'on'
    )

    sendOtherEvents(response)
}

def on() {
    if (logEnable) log.debug("attempting to turn on switch via WebRelay: ${address}")
    def uri = "http://${address}"
    def path = '/stateFull.xml'
    def queryString = 'relayState=1'

    response = doHttpGet(uri, path, queryString)

    sendEvent(
        name: 'switch',
        value: 'on'
    )

    sendOtherEvents(response)
}

def off() {
    if (logEnable) log.debug("attempting to turn off switch via WebRelay: ${address}")
    def uri = "http://${address}"
    def path = '/stateFull.xml'
    def queryString = 'relayState=0'

    response = doHttpGet(uri, path, queryString)

    sendEvent(
        name: 'switch',
        value: 'off'
    )

    sendOtherEvents(response)
}

def sendOtherEvents(response) {
   def rebootStates = ["Auto Reboot off", "Pinging", "Waiting for response", "Rebooting", "Waiting for boot"]

    sendEvent(
        name: 'relaystate',
        value: (response.relaystate == '0') ? 'off' : 'on'
    )
    sendEvent(
        name: 'inputstate',
        value: (response.inputstate == '0') ? 'off' : 'on'
    )
    sendEvent(
        name: 'rebootstate',
        value: rebootStates[response.rebootstate.toInteger()]
    )
    sendEvent(
        name: 'totalreboots',
        value: response.totalreboots
    )
}

def doHttpGet(uri, path, queryString = null) {
    if (logEnable) log.debug("doHttpGet called: uri: ${uri} path: ${path} queryString: ${queryString}")
    def responseData = []
    int retries = 0
    def cmds = []
    cmds << 'delay 1'

    // Attempt a max of 3 retries to address cases in which transient read errors can occur when 
    // interacting with the WebRelay.
    while(retries++ < 3) {
        try {
            httpGet( [uri: uri, path: path, queryString: queryString, contentType: "text/xml"] ) { response ->
                if (logEnable) log.debug "status:      ${response.getStatus()}"
		        if (logEnable) log.debug "context:     ${response.getContext()}"
		        if (logEnable) log.debug "entity:      ${response.getEntity()}"
		        if (logEnable) log.debug "locale:      ${response.getLocale()}"
		        if (logEnable) log.debug "statusLine:  ${response.getStatusLine()}"
		        if (logEnable) log.debug "contentType: ${response.getContentType()}"
		        if (logEnable) log.debug "data:        ${response.getData()}"
                if (response.success) {
                    responseData = response.data
                    if (logEnable) log.debug "relaystate:   ${response.data.relaystate}"
                    if (logEnable) log.debug "inputstate:   ${response.data.inputstate}"
                    if (logEnable) log.debug "rebootstate:  ${response.data.rebootstate}"
                    if (logEnable) log.debug "totalreboots: ${response.data.totalreboots}"
                } else {
                    throw new Exception("httpGet() not successful for: ${uri} ${path}") 
                }
            }
            return(responseData)
        } catch (Exception e) {
            log.warn "httpGet() of ${path} to WebRelay failed: ${e.message}"
            // When read time out error occurs, retry the operation. Otherwise, throw
            // an exception.
            if (!e.message.contains('Read timed out')) throw new Exception("httpGet() failed for: ${uri} ${path}")
        }
        log.warn('Delaying 1 second before next httpGet() retry')
        cmds
    }
    throw new Exception("httpGet() exceeded max retries for: ${uri} ${path}")
}