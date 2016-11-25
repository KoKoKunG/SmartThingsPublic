/**
 *  Hombot Remote
 *
 *  Copyright 2016 Ian Mascarenhas
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
metadata {
	definition (name: "Hombot Remote", namespace: "ianm", author: "Ian Mascarenhas") {
		capability "Battery"
		capability "Polling"
		capability "Refresh"
        
        command "turboCommand"
        command "modeCommand"
        command "repeatCommand"
        command "startCommand"
        command "pauseCommand"
        command "homeCommand"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles {
        standardTile("refresh", "device.switch", decoration: "flat") {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}	
        valueTile("power", "device.battery", decoration: "flat") {
			state "default", label:'Battery ${currentValue}%'
		}
		valueTile("state", "device.state", decoration: "flat") {
			state "default", label:'${currentValue}'
		}
        standardTile("turbo", "device.turbo", decoration: "flat") {
            state "true", label: 'Turbo On', action: "turboCommand"//, backgroundColor: "#ffffff"
			state "false", label: 'Turbo Off', action: "turboCommand"//, backgroundColor: "#79b821"
		}
		standardTile("repeat", "device.repeat", decoration: "flat") {
            state "true", label: 'Repeat On', action: "repeatCommand"//, backgroundColor: "#ffffff"
			state "false", label: 'Repeat Off', action: "repeatCommand"//, backgroundColor: "#79b821"
		}
        standardTile("mode", "device.mode", decoration: "flat") {
			state "default", label:'Mode ${currentValue}', action: "modeCommand"
		}
		valueTile("version", "device.version", decoration: "flat") {
			state "default", label:'Firmware Ver. ${currentValue}'
		}
		standardTile("home", "device.home", decoration: "flat") {
			state "default", label:"", action:"homeCommand", icon:"st.Home.home2"
		}	
		standardTile("start", "device.start", decoration: "flat") {
			state "default", label:"START", action:"startCommand"//, icon:"st.Home.home2"
		}
     	standardTile("pause", "device.pause", decoration: "flat") {
			state "default", label:"PAUSE", action:"pauseCommand"//, icon:"st.Home.home2"
		}	       
  	}
    
    main "state"
		details (["refresh","state","power","turbo","repeat","mode","home","start","pause","version"])
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
	// TODO: handle 'battery' attribute
    
    def msg = parseLanMessage(description)
    
    def headersAsString = msg.header // => headers as a string
    def headerMap = msg.headers      // => headers as a Map
    def body = msg.body              // => request body as a string
    def status = msg.status          // => http status code of the response
    def json = msg.json              // => any JSON included in response body, as a data structure of lists and maps
    def xml = msg.xml                // => any XML included in response body, as a document tree structure
    def data = msg.data              // => either JSON or XML in response body (whichever is specified by content-type header in response)

	log.debug "headerMap '${headerMap}'"
    log.debug "body '${body}'"
    log.debug "json '${json}'"
  
    def dataList = []
    def statusMap = [:]
 
    if( !body ) {
        println "Page does not exist"
    } else {
    	dataList = body.split('\n')
        log.debug "dataList '${dataList}'"
        
        dataList.each { statusString ->
            statusMap.put(statusString.split('=')[0], statusString.split('"')[1])
        }
        log.debug "statusMap '${statusMap}'" 
        
        if (headerMap.'content-type' == "text/plain") {
            sendEvent(name: "battery", value: statusMap.JSON_BATTPERC)
            sendEvent(name: "state", value: statusMap.JSON_ROBOT_STATE)
            sendEvent(name: "turbo", value: statusMap.JSON_TURBO)
            sendEvent(name: "repeat", value: statusMap.JSON_REPEAT)
            sendEvent(name: "mode", value: statusMap.JSON_MODE)
            sendEvent(name: "version", value: statusMap.JSON_VERSION)
        } else {
        	//log.debug "not status response"
            statusCommand()
        }
    }
	//log.debug "dataList '${dataList}'"  
}

def setState(){
	
}

// handle commands
def poll() {
	log.debug "Executing 'poll'"
    statusCommand()
	// TODO: handle 'poll' command
}

def refresh() {
	log.debug "Executing 'refresh'"
    statusCommand()
	// TODO: handle 'refresh' command  http://192.168.1.154:6260/status.txt
}

def myCommand() {
    def result = new physicalgraph.device.HubAction(
        method: "GET",
        path: "/status.txt",
        headers: [
            HOST: getHostAddress()
        ]
    )
    log.debug "myCommand result : $result"
    return result
}


def statusCommand(){
	log.debug "Executing 'statusCommand'"
    
    def result = new physicalgraph.device.HubAction(
        method: "GET",
        path: "/status.txt",
        headers: [
            HOST: getHostAddress()
        ]
    )
    log.debug "statusCommand result : $result"
    return result
}

def modeCommand(){
	log.debug "Executing 'modeCommand'"
    
    def result = new physicalgraph.device.HubAction(
        method: "GET",
        path: "/json.cgi?mode",
        headers: [
            HOST: getHostAddress()
        ]
    )
    log.debug "statusCommand result : $result"
    return result
}

def repeatCommand(){
	log.debug "Executing 'repeatCommand'"
    
    def result = new physicalgraph.device.HubAction(
        method: "GET",
        path: "/json.cgi?repeat",
        headers: [
            HOST: getHostAddress()
        ]
    )
    log.debug "repeatCommand result : $result"
    return result
}

def turboCommand(){
	log.debug "Executing 'turboCommand'"
        
    def result = new physicalgraph.device.HubAction(
        method: "GET",
        path: "/json.cgi?turbo",
        headers: [
            HOST: getHostAddress()
        ]
    )
    log.debug "turboCommand result : $result"
    return result
}

def homeCommand(){
	log.debug "Executing 'homeCommand'"
        
    def result = new physicalgraph.device.HubAction(
        method: "GET",
        path: "/json.cgi?%7b%22COMMAND%22:%22HOMING%22%7d",
        headers: [
            HOST: getHostAddress()
        ]
    )
    log.debug "homeCommand result : $result"
    return result
}

def startCommand(){
	log.debug "Executing 'startCommand'"
    
    def result = new physicalgraph.device.HubAction(
        method: "GET",
        path: "/json.cgi?%7b%22COMMAND%22:%22CLEAN_START%22%7d",
        headers: [
            HOST: getHostAddress()
        ]
    )
    log.debug "startCommand result : $result"
    return result
}

def pauseCommand(){
	log.debug "Executing 'pauseCommand'"
    
    def result = new physicalgraph.device.HubAction(
        method: "GET",
        path: "/json.cgi?%7b%22COMMAND%22:%22PAUSE%22%7d",
        headers: [
            HOST: getHostAddress()
        ]
    )
    log.debug "pauseCommand result : $result"
    return result
}



private Long converIntToLong(ipAddress) {
	log.debug(ipAddress)
	long result = 0;;
	def parts = ipAddress.split("\\.")
    for (int i = 3; i >= 0; i--) {
        result |= (Long.parseLong(parts[3 - i]) << (i * 8));
    }

    return result & 0xFFFFFFFF;
}

private String convertIPToHex(ipAddress) {
	return Long.toHexString(converIntToLong(ipAddress));
}

private String getDeviceId() {
	def ip = convertIPToHex(settings.server)
	def port = Long.toHexString(Long.parseLong(settings.port))
	return ip + ":0x" + port
}


def sendHttp() {
	log.debug "Executing 'sendHttp'"
    def ip = "${settings.server}:${settings.port}"
    log.debug "Using IP: $ip"
    def deviceNetworkId = "1234"
    sendHubCommand(new physicalgraph.device.HubAction("""GET /?${settings.params} HTTP/1.1\r\nHOST: $ip\r\n\r\n""", physicalgraph.device.Protocol.LAN, "${deviceNetworkId}"))
}


// gets the address of the device
private getHostAddress() {
    def ip = getDataValue("ip")
    def port = getDataValue("port")

    if (!ip || !port) {
        def parts = device.deviceNetworkId.split(":")
        if (parts.length == 2) {
            ip = parts[0]
            port = parts[1]
        } else {
            log.warn "Can't figure out ip and port for device: ${device.id}"
        }
    }

    log.debug "Using IP: $ip and port: $port for device: ${device.id}"
    return convertHexToIP(ip) + ":" + convertHexToInt(port)
}

private Integer convertHexToInt(hex) {
    return Integer.parseInt(hex,16)
}

private String convertHexToIP(hex) {
    return [convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}