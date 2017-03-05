/**
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

metadata
{
    definition (name: "GoControl Bulb Programmer", namespace: "alliv", author: "Aleksandr Livhshits") 
    {
        capability "Configuration" 
        
        command "updateSettings"
    }
    preferences
    {
        input ( "memory", "number", title: "Dim Level Memory ", defaultValue: 0, range: "0..1", required: true)
        input ( "step", "number", title: " Dim/Bright Step Level ", defaultValue: 1, range: "1..99", required: true)
        input ( "speed", "number", title: "Dim/Bright Speed", defaultValue: 3, range: "1..10", required: true)
    }
    
    tiles(scale: 2)
    {
        standardTile("updateSettings", "device.updateSettings", height: 2, width: 2, inactiveLabel: false, decoration: "flat")
        {
            state "default" , action:"updateSettings", icon:"st.secondary.configure"
        }
        main(["updateSettings"])
    }
}

def parse(String description)
{
	log.debug zwave.parse(description)
}

def updateSettings()
{
    log.debug("Updating Bulb Settings")
    
    def cmds = []
    cmds << zwave.configurationV1.configurationSet(configurationValue: [memory], parameterNumber: 1, size: 1).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [step], parameterNumber: 9, size: 1).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [speed], parameterNumber: 10, size: 1).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 1).format()
    cmds << zwave.configurationV2.configurationGet(parameterNumber: 9).format()
    cmds << zwave.configurationV2.configurationGet(parameterNumber: 10).format()
    
    delayBetween(cmds, 1000)
}