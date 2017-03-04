
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
    definition (name: "Leviton Dimmer Programmer", namespace: "alliv", author: "Aleksandr Livhshits") 
    {
        capability "Configuration" 
        
        command "updateSettings"
        
        fingerprint inClusters: "0x26"
    }
    preferences
    {
        input ( "loadType", "number", title: "Load Type", defaultValue: 10, range: "0..3", required: true)
        input ( "fadeOn", "number", title: "Fade On Time", defaultValue: 0, range: "0..253", required: true)
        input ( "fadeOff", "number", title: "Fade On Time", defaultValue: 0, range: "0..253", required: true)
        input ( "minLevel", "number", title: "Minimum Level", defaultValue: 10, range: "0..100", required: true)
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
def updateSettings()
{
    log.debug("Updating Switch Settings")
    
    def cmds = []
    cmds << zwave.configurationV1.configurationSet(configurationValue: [0], parameterNumber: 1, size: 1).format()
    cmds << zwave.configurationV1.configurationSet(configurationValue: [0], parameterNumber: 2, size: 1).format()
    
    delayBetween(cmds, 500)
}

