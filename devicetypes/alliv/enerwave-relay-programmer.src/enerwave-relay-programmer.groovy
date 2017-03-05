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
    definition (name: "EnerWave Relay Programmer", namespace: "alliv", author: "Aleksandr Livhshits") 
    {
        capability "Configuration" 
        
        command "updateSettings"
    }
    preferences
    {
        input ( "hail", "number", title: "Send Hail mode", defaultValue: 0, range: "0..1", required: true)
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
    log.debug("Updating Relay Settings")
    
    def cmds = []
    cmds << zwave.manufacturerSpecificV1.manufacturerSpecificGet().format()
	cmds << zwave.configurationV1.configurationSet(configurationValue: [hail], parameterNumber: 3, size: 1).format()
    cmds << zwave.configurationV1.configurationGet(parameterNumber: 3).format()
    
    delayBetween(cmds, 1000)
}