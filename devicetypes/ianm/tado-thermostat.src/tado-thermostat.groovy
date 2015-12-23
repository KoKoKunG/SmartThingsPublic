/**
 *  Copyright 2015 SmartThings
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
 *	Tado Thermostat
 *
 *	Author: Ian M
 *
 *	Updates: 
 *	2015-12-23	Added functionality to change thermostat settings
 *	2015-12-04	Initial release
 */
 
preferences {
	input("username", "text", title: "Username", description: "Your Tado username")
	input("password", "password", title: "Password", description: "Your Tado password")
}  
 
metadata {
	definition (name: "Tado Thermostat", namespace: "ianm", author: "Ian M") {
		capability "Actuator"
        capability "Temperature Measurement"
		capability "Thermostat"
        capability "Presence Sensor"
		capability "Polling"
		capability "Refresh"
        capability "Switch"
        
        command "heatingSetpointUp"
        command "heatingSetpointDown"
	}

	// simulator metadata
	simulator {
		// status messages

		// reply messages
	}

	tiles(scale: 2){
      	multiAttributeTile(name: "thermostat", width: 6, height: 4, type:"lighting") {
			tileAttribute("device.temperature", key:"PRIMARY_CONTROL", canChangeIcon: true){
            	attributeState "default", label:'${currentValue}° C', unit:"C", backgroundColor:"#fab907", icon:"st.Home.home1"
            }
            tileAttribute ("thermostatOperatingState", key: "SECONDARY_CONTROL") {
				attributeState "thermostatOperatingState", label:'${currentValue}'
			}
		}
        
        valueTile("heatingSetpoint", "device.heatingSetpoint", width: 2, height: 1, decoration: "flat") {
			state("default", label: '${currentValue}° C')
		}

        standardTile("thermostatMode", "device.thermostatMode", width: 2, height: 2) {
            state "HOME", label:'${name}', backgroundColor:"#fab907", icon:"st.Home.home2"
            state "AWAY", label:'${name}', backgroundColor:"#62aa12", icon:"st.Outdoor.outdoor18"
            state "SLEEP", label:'${name}', backgroundColor:"#0164a8", icon:"st.Bedroom.bedroom2"
            state "OFF", label:'${name}', backgroundColor:"#ffffff", icon:"st.switches.switch.off"
            state "MANUAL", label:'${name}', backgroundColor:"#ffffff", icon:"st.Weather.weather1"
		}
        
        standardTile("presence", "device.presence", width: 2, height: 2) {
			state "present", labelIcon:"st.presence.tile.present", backgroundColor:"#53a7c0"
			state "not present", labelIcon:"st.presence.tile.not-present", backgroundColor:"#ebeef2"
		}
      	
        standardTile("refresh", "device.switch", width: 2, height: 2, decoration: "flat") {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		
        standardTile("setAuto", "device.thermostat", width: 2, height: 1, decoration: "flat") {
			state "default", label:"Auto", action:"thermostat.auto"
		}

        standardTile("setManual", "device.thermostat", width: 2, height: 1, decoration: "flat") {
			state "default", label:"Manual", action:"thermostat.heat"
		}

        standardTile("setOff", "device.thermostat", width: 2, height: 1, decoration: "flat") {
			state "default", label:"Off", action:"thermostat.off"
		}
        
        standardTile("heatingSetpointUp", "device.heatingSetpoint", canChangeIcon: false, decoration: "flat") {
            state "heatingSetpointUp", label:'  ', action:"heatingSetpointUp", icon:"st.thermostat.thermostat-up"
        }

        standardTile("heatingSetpointDown", "device.heatingSetpoint", canChangeIcon: false, decoration: "flat") {
            state "heatingSetpointDown", label:'  ', action:"heatingSetpointDown", icon:"st.thermostat.thermostat-down", backgroundColor:"#bc2323"
        }

		main "thermostat"
		details (["thermostat","refresh","heatingSetpoint","thermostatMode","heatingSetpointUp","heatingSetpointDown","setAuto","setManual","setOff"])
	}
}

// Parse incoming device messages to generate events
private parseResponse(resp) {
    log.debug("Executing parseResponse: "+resp.data)
    //log.debug("Output success: "+resp.data.success)
    log.debug("Output status: "+resp.status)
    if(resp.status == 200) {
    	log.debug("Executing parseResponse.successTrue")
        
        def temperature = Math.round(resp.data.insideTemp)
        log.debug("Read temperature: " + temperature)
        
        def controlPhase = resp.data.controlPhase
        if(resp.data.controlPhase == "UNDEFINED"){
        	controlPhase = "FROST PROTECTION"
        }
        log.debug("Read controlPhase1: " + controlPhase)
        
        def setPointTemp 
        if (resp.data.setPointTemp != null){
        	setPointTemp = Math.round(resp.data.setPointTemp)
        }else{
        	setPointTemp = "--"
        }
        log.debug("Read setPointTemp: " + setPointTemp)
        
        def autoOperation = resp.data.autoOperation
        if(resp.data.operation == "NO_FREEZE"){
        	autoOperation = "OFF"
        }else if(resp.data.operation == "MANUAL"){
        	autoOperation = "MANUAL"
        }
        log.debug("Read autoOperation: " + autoOperation)
        
        def presence
        if(resp.data.operation == "AWAY"){
        	presence = "not present"
        }else {
        	presence = "present"
        }
        log.debug("Read presence: " + presence)
        
        def temperatureUnit = "C"
        
        sendEvent(name: 'temperature', value: temperature, unit: temperatureUnit)
        sendEvent(name: 'heatingSetpoint', value: setPointTemp, unit: temperatureUnit)
		sendEvent(name: 'thermostatOperatingState', value: controlPhase)
        sendEvent(name: 'thermostatMode', value: autoOperation)       
        sendEvent(name: 'presence', value: presence)
    }else if(resp.status == 201){
        log.debug("Something was created/updated")
    }
}


def poll() {
	log.debug "Executing 'poll'"
	refresh()
}

def refresh() {
	log.debug "Executing 'refresh'"
    sendCommand("getCurrentState")
}

def auto() {
	log.debug "Executing 'auto'"
	autoCommand()
    refresh()
}

def off() {
	log.debug "Executing 'off'"
	offCommand()
    refresh()
}

def heat() {
	log.debug "Executing 'heat'"
	manualCommand()
    refresh()
}

def setHeatingSetpoint(targetTemperature) {
	log.debug "Executing 'setHeatingSetpoint'"
    log.debug "Target Temperature ${targetTemperature}"
    setTempCommand(targetTemperature)
	refresh()
}

def heatingSetpointUp(){
	int newSetpoint = device.currentValue("heatingSetpoint") + 1
	log.debug "Setting heatingSetpoint up to: ${newSetpoint}"
	setHeatingSetpoint(newSetpoint)
}

def heatingSetpointDown(){
	int newSetpoint = device.currentValue("heatingSetpoint") - 1
	log.debug "Setting heatingSetpoint down to: ${newSetpoint}"
	setHeatingSetpoint(newSetpoint)
}



private sendCommand(path, method="GET", body=null) {
    //def accessToken = getAccessToken()
    def pollParams = [
        uri: "https://my.tado.com",
        path: "/mobile/1.9/getCurrentState",
        requestContentType: "application/json",
    	query: [username:settings.username, password:settings.password],
        body: body
    ]
    log.debug(method+" Http Params ("+pollParams+")")
    
    try{
        if(method=="GET"){
        	log.debug "Executing 'sendCommand.GET'"
            httpGet(pollParams) { resp ->            
                //log.debug resp.data
                parseResponse(resp)
            }
        }else if(method=="PUT") {
        	log.debug "Executing 'sendCommand.PUT'"
            httpPut(pollParams) { resp ->            
                parseResponse(resp)
            }
        }
    } catch(Exception e){
        debug("___exception: " + e)
    }
}



// Commands to device

def autoCommand() {
    def method = "GET"
    def pollParams = [
        uri: "https://my.tado.com",
        path: "/mobile/1.9/updateThermostatSettings",
        requestContentType: "application/json",
    	query: [username:settings.username, password:settings.password, setMode:"AUTO"],
        body: null
    ]
    
    log.debug(method+" Http Params ("+pollParams+")")
    
    try{
        log.debug "Executing 'sendCommand.setAuto'"
        httpGet(pollParams) { resp ->            
            //log.debug resp.data
            //parseResponse(resp)
        }        
    } catch(Exception e){
    	debug("___exception: " + e)
    }
}

def offCommand() {
    def method = "GET"
    def pollParams = [
        uri: "https://my.tado.com",
        path: "/mobile/1.9/updateThermostatSettings",
        requestContentType: "application/json",
    	query: [username:settings.username, password:settings.password, setMode:"NO_FREEZE"],
        body: null
    ]
    
    log.debug(method+" Http Params ("+pollParams+")")
    
    try{
        log.debug "Executing 'sendCommand.setOff'"
        httpGet(pollParams) { resp ->            
            //log.debug resp.data
            //parseResponse(resp)
        }        
    } catch(Exception e){
    	debug("___exception: " + e)
    }
}

def manualCommand() {
    def method = "GET"
    def pollParams = [
        uri: "https://my.tado.com",
        path: "/mobile/1.9/updateThermostatSettings",
        requestContentType: "application/json",
    	query: [username:settings.username, password:settings.password, setMode:"MANUAL"],
        body: null
    ]
    
    log.debug(method+" Http Params ("+pollParams+")")
    
    try{
        log.debug "Executing 'sendCommand.setManual'"
        httpGet(pollParams) { resp ->            
            //log.debug resp.data
            //parseResponse(resp)
        }        
    } catch(Exception e){
    	debug("___exception: " + e)
    }
}

def setTempCommand(targetTemperature) {
    def method = "GET"
    def pollParams = [
        uri: "https://my.tado.com",
        path: "/mobile/1.9/updateThermostatSettings",
        requestContentType: "application/json",
    	query: [username:settings.username, password:settings.password, setMode:"MANUAL", manualTemp:targetTemperature],
        body: null
    ]
    
    log.debug(method+" Http Params ("+pollParams+")")
    
    try{
        log.debug "Executing 'sendCommand.setTempCommand' to ${targetTemperature}"
        httpGet(pollParams) { resp ->            
            //log.debug resp.data
            //parseResponse(resp)
        }        
    } catch(Exception e){
    	debug("___exception: " + e)
    }
}