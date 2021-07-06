/****************************************************************************
 *
 * (c) 2009-2019 QGROUNDCONTROL PROJECT <http://www.qgroundcontrol.org>
 *
 * QGroundControl is licensed according to the terms in the file
 * COPYING.md in the root of the source code directory.
 *
 * @file
 *   @brief Custom Autopilot Plugin
 *   @author Gus Grubba <gus@auterion.com>
 */

#include "CustomAutoPilotPlugin.h"

#include "UAS.h"
#include "APM/APMParameterMetaData.h"
#include "APM/APMFirmwarePlugin.h"
#include "APM/ArduCopterFirmwarePlugin.h"
#include "APM/ArduRoverFirmwarePlugin.h"
#include "VehicleComponent.h"
#include "APM/APMAirframeComponent.h"
#include "APM/APMFlightModesComponent.h"
#include "APM/APMRadioComponent.h"
#include "APM/APMSafetyComponent.h"
#include "APM/APMTuningComponent.h"
#include "APM/APMSensorsComponent.h"
#include "APM/APMPowerComponent.h"
#include "APM/APMMotorComponent.h"
#include "APM/APMCameraComponent.h"
#include "APM/APMLightsComponent.h"
#include "APM/APMSubFrameComponent.h"
#include "APM/APMFollowComponent.h"
#include "ESP8266Component.h"
#include "APM/APMHeliComponent.h"
#include "QGCApplication.h"
#include "ParameterManager.h"

#include "QGCApplication.h"
#include "QGCCorePlugin.h"

//-----------------------------------------------------------------------------
CustomAutoPilotPlugin::CustomAutoPilotPlugin(Vehicle* vehicle, QObject* parent)
    : AutoPilotPlugin(vehicle, parent)
    , _incorrectParameterVersion(false)
    , _airframeComponent        (nullptr)
    , _cameraComponent          (nullptr)
    , _lightsComponent          (nullptr)
    , _subFrameComponent        (nullptr)
    , _flightModesComponent     (nullptr)
    , _powerComponent           (nullptr)
    , _motorComponent           (nullptr)
    , _radioComponent           (nullptr)
    , _safetyComponent          (nullptr)
    , _sensorsComponent         (nullptr)
    , _tuningComponent          (nullptr)
    , _esp8266Component         (nullptr)
    , _heliComponent            (nullptr)
    , _followComponent          (nullptr)
{
    //connect(qgcApp()->toolbox()->corePlugin(), &QGCCorePlugin::showAdvancedUIChanged, this, &CustomAutoPilotPlugin::_advancedChanged);
}


CustomAutoPilotPlugin::~CustomAutoPilotPlugin()
{

}

//-----------------------------------------------------------------------------
//void
//CustomAutoPilotPlugin::_advancedChanged(bool)
//{
//    _components.clear();
//    emit vehicleComponentsChanged();
//}

//-----------------------------------------------------------------------------
const QVariantList&
CustomAutoPilotPlugin::vehicleComponents()
{
    if (_components.count() == 0 && !_incorrectParameterVersion) {
        if (_vehicle) {
            bool showAdvanced = qgcApp()->toolbox()->corePlugin()->showAdvancedUI();
            qDebug() << "Loading components:" << showAdvanced;
            if (_vehicle->parameterManager()->parametersReady()) {
                if(showAdvanced) {
                _airframeComponent = new APMAirframeComponent(_vehicle, this);
                _airframeComponent->setupTriggerSignals();
                _components.append(QVariant::fromValue(reinterpret_cast<VehicleComponent*>(_airframeComponent)));
                }
                if (!_vehicle->hilMode()) {
                    _sensorsComponent = new APMSensorsComponent(_vehicle, this);
                    _sensorsComponent->setupTriggerSignals();
                    _components.append(QVariant::fromValue(reinterpret_cast<VehicleComponent*>(_sensorsComponent)));
                }
                if(showAdvanced) {
                    _radioComponent = new APMRadioComponent(_vehicle, this);
                    _radioComponent->setupTriggerSignals();
                    _components.append(QVariant::fromValue(reinterpret_cast<VehicleComponent*>(_radioComponent)));

                    _flightModesComponent = new APMFlightModesComponent(_vehicle, this);
                    _flightModesComponent->setupTriggerSignals();
                    _components.append(QVariant::fromValue(reinterpret_cast<VehicleComponent*>(_flightModesComponent)));

                    _powerComponent = new APMPowerComponent(_vehicle, this);
                    _powerComponent->setupTriggerSignals();
                    _components.append(QVariant::fromValue(reinterpret_cast<VehicleComponent*>(_powerComponent)));

                    _motorComponent = new APMMotorComponent(_vehicle, this);
                    _motorComponent->setupTriggerSignals();
                    _components.append(QVariant::fromValue(reinterpret_cast<VehicleComponent*>(_motorComponent)));
                }

                _safetyComponent = new APMSafetyComponent(_vehicle, this);
                _safetyComponent->setupTriggerSignals();
                _components.append(QVariant::fromValue(reinterpret_cast<VehicleComponent*>(_safetyComponent)));

                if(showAdvanced) {
                    _tuningComponent = new APMTuningComponent(_vehicle, this);
                    _tuningComponent->setupTriggerSignals();
                    _components.append(QVariant::fromValue(reinterpret_cast<VehicleComponent*>(_tuningComponent)));

                    //-- Is there support for cameras?
                    if(_vehicle->parameterManager()->parameterExists(_vehicle->id(), "TRIG_MODE")) {
                        _cameraComponent = new APMCameraComponent(_vehicle, this);
                        _cameraComponent->setupTriggerSignals();
                        _components.append(QVariant::fromValue(reinterpret_cast<VehicleComponent*>(_cameraComponent)));
                    }
                }

                //-- Is there an ESP8266 Connected?
                if(_vehicle->parameterManager()->parameterExists(MAV_COMP_ID_UDP_BRIDGE, "SW_VER")) {
                    _esp8266Component = new ESP8266Component(_vehicle, this);
                    _esp8266Component->setupTriggerSignals();
                    _components.append(QVariant::fromValue(reinterpret_cast<VehicleComponent*>(_esp8266Component)));
                }
            } else {
                qWarning() << "Call to vehicleCompenents prior to parametersReady";
            }

//            if(_vehicle->parameterManager()->parameterExists(_vehicle->id(), "SLNK_RADIO_CHAN")) {
//                _syslinkComponent = new SyslinkComponent(_vehicle, this);
//                _syslinkComponent->setupTriggerSignals();
//                _components.append(QVariant::fromValue(reinterpret_cast<VehicleComponent*>(_syslinkComponent)));
//            }
        } else {
            qWarning() << "Internal error";
        }
    }
    return _components;
}

QString CustomAutoPilotPlugin::prerequisiteSetup(VehicleComponent* component) const
{
    bool requiresAirframeCheck = false;

    if (qobject_cast<const APMFlightModesComponent*>(component)) {
        if (_airframeComponent && !_airframeComponent->setupComplete()) {
            return _airframeComponent->name();
        }
        if (_radioComponent && !_radioComponent->setupComplete()) {
            return _radioComponent->name();
        }
        requiresAirframeCheck = true;
    } else if (qobject_cast<const APMRadioComponent*>(component)) {
        requiresAirframeCheck = true;
    } else if (qobject_cast<const APMCameraComponent*>(component)) {
        requiresAirframeCheck = true;
    } else if (qobject_cast<const APMPowerComponent*>(component)) {
        requiresAirframeCheck = true;
    } else if (qobject_cast<const APMSafetyComponent*>(component)) {
        requiresAirframeCheck = true;
    } else if (qobject_cast<const APMTuningComponent*>(component)) {
        requiresAirframeCheck = true;
    } else if (qobject_cast<const APMSensorsComponent*>(component)) {
        requiresAirframeCheck = true;
    }

    if (requiresAirframeCheck) {
        if (_airframeComponent && !_airframeComponent->setupComplete()) {
            return _airframeComponent->name();
        }
    }

    return QString();
}
