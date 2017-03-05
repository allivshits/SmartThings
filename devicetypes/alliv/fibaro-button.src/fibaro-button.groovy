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
    definition(name: "Fibaro Button", namespace: "alliv", author: "Aleksandr Livshits")
    {
        capability "Actuator"
        capability "Button"
        capability "Battery"
        capability "Configuration"

        fingerprint deviceId: "0x1801", inClusters: "0x5E, 0x86, 0x72, 0x5B, 0x5A, 0x59, 0x85, 0x73, 0x84, 0x80, 0x71, 0x56, 0x70, 0x8E, 0x7A, 0x98", outClusters: "0x26, 0x9C"
    }

    tiles(scale: 2)
    {
        multiAttributeTile(name: "button", type: "generic", width: 6, height: 4)
        {
            tileAttribute("device.button", key: "PRIMARY_CONTROL")
            {
                attributeState "default", label: 'Fibaro Button', backgroundColor: "#44b621", icon: "st.Home.home30"
            }
            tileAttribute("device.battery", key: "SECONDARY_CONTROL")
            {
                attributeState "battery", label: '${currentValue} % battery'
            }
        }
        valueTile("configure", "device.button", width: 2, height: 2, decoration: "flat")
        {
            state "default", backgroundColor: "#ffffff", action: "setupBonds", icon: "st.secondary.configure"
        }

        main "button"
        details(["button", "configure"])
    }
}

def parse(String description)
{
    def cmd = zwave.parse(description)
    if (cmd)
    {
        return zwaveEvent(cmd)
    }
}

def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd)
{
    def encapsulatedCommand = cmd.encapsulatedCommand()
    if (encapsulatedCommand)
    {
        return zwaveEvent(encapsulatedCommand)
    }
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv1.WakeUpNotification cmd)
{
    log.debug("Button Woke Up!")
    def results = [createEvent(descriptionText: "$device.displayName woke up", isStateChange: false)]
    def prevBattery = device.currentState("battery")
    def stale = false
    if (!prevBattery)
    {
        stale = true
    }
    if (!stale && java.time.temporal.ChronoUnit.HOURS.between(new Date(), prevBattery.date) >= 6)
    {
        log.debug "Last battery report: $prevBattery.date"
        stale = true
    }
    if (stale)
    {
        results << secure(zwave.batteryV1.batteryGet())
    }
    results << response(secure(zwave.wakeUpV1.wakeUpNoMoreInformation()))
    return results
}

def zwaveEvent(physicalgraph.zwave.commands.centralscenev1.CentralSceneNotification cmd)
{
    log.debug "keyAttributes: $cmd.keyAttributes"
    log.debug "sceneNumber: $cmd.sceneNumber"
    log.debug "sequenceNumber: $cmd.sequenceNumber"
    log.debug "payload: $cmd.payload"

    def bttn = $cmd.keyAttributes == 0 ? 1 : $cmd.keyAttributes - 1

    createEvent(name: "button", value: "pushed", data: [buttonNumber: $bttn], descriptionText: $/$device.displayName button $bttn was pushed/$, isStateChange: true)
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd)
{
    def map = [name: "battery", unit: "%"]
    if (cmd.batteryLevel == 0xFF)
    {
        map.value = 1
        map.descriptionText = "${device.displayName} has a low battery"
    }
    else
    {
        map.value = cmd.batteryLevel
    }
    createEvent(map)
}
def secure(physicalgraph.zwave.Command cmd) {
    zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
}

def execCommands(commands, delay=200) {
    delayBetween(commands.collect{ secure(it) }, delay)
}

def removeBonds()
{
    execCommands([secure(zwave.associationV1.associationGet(groupingIdentifier: 2 ))])
}

def zwaveEvent(physicalgraph.zwave.commands.associationv1.AssociationReport cmd)
{
    log.debug "Associated nodes: $cmd.nodeid"
}


def setupBonds()
{
    def cmds = []
    cmds << zwave.associationV1.associationSet(groupingIdentifier: 2, nodeId: 0x06)
    cmds << zwave.associationV1.associationSet(groupingIdentifier: 2, nodeId: 0x0C)
    execCommands(cmds)
}

def configure()
{
    sendEvent(name: "numberOfButtons", value: 5)

    def cmds = []
    cmds << zwave.associationV1.associationSet(groupingIdentifier: 1, nodeId: zwaveHubNodeId)
    cmds << zwave.configurationV1.configurationSet(configurationValue: [31], parameterNumber: 1, size: 1)
    cmds << zwave.configurationV1.configurationSet(configurationValue: [0], parameterNumber: 3, size: 1)
    cmds << zwave.configurationV1.configurationSet(configurationValue: [3], parameterNumber: 10, size: 1)
    cmds << zwave.configurationV1.configurationSet(configurationValue: [255], parameterNumber: 11, size: 1)
    cmds << zwave.configurationV1.configurationSet(configurationValue: [0], parameterNumber: 12, size: 1)
    cmds << zwave.configurationV1.configurationSet(configurationValue: [0], parameterNumber: 14, size: 1)
    cmds << zwave.configurationV1.configurationSet(configurationValue: [0], parameterNumber: 20, size: 1)
    cmds << zwave.configurationV1.configurationSet(configurationValue: [0], parameterNumber: 22, size: 1)
    cmds << zwave.configurationV1.configurationSet(configurationValue: [0], parameterNumber: 24, size: 1)
    cmds << zwave.configurationV1.configurationSet(configurationValue: [0], parameterNumber: 29, size: 1)
    cmds << zwave.configurationV1.configurationSet(configurationValue: [0], parameterNumber: 30, size: 1)

    log.debug "Resetting Sensor Parameters"
    execCommands(cmds)
}
