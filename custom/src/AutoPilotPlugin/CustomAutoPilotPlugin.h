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

#pragma once

#include "AutoPilotPlugin.h"
#include "Vehicle.h"

class APMAirframeComponent;
class APMFlightModesComponent;
class APMRadioComponent;
class APMTuningComponent;
class APMSafetyComponent;
class APMSensorsComponent;
class APMPowerComponent;
class APMMotorComponent;
class APMCameraComponent;
class APMLightsComponent;
class APMSubFrameComponent;
class ESP8266Component;
class APMHeliComponent;
class APMFollowComponent;


/// Custom overrides from standard PX4AutoPilotPlugin implementation
class CustomAutoPilotPlugin : public AutoPilotPlugin
{
    Q_OBJECT
public:
    CustomAutoPilotPlugin(Vehicle* vehicle, QObject* parent);
    ~CustomAutoPilotPlugin();

    const QVariantList& vehicleComponents() override;
    QString prerequisiteSetup(VehicleComponent* component) const override;

protected:
    bool                        _incorrectParameterVersion; ///< true: parameter version incorrect, setup not allowed
    APMAirframeComponent*       _airframeComponent;
    APMCameraComponent*         _cameraComponent;
    APMLightsComponent*         _lightsComponent;
    APMSubFrameComponent*       _subFrameComponent;
    APMFlightModesComponent*    _flightModesComponent;
    APMPowerComponent*          _powerComponent;
    APMMotorComponent*          _motorComponent;
    APMRadioComponent*          _radioComponent;
    APMSafetyComponent*         _safetyComponent;
    APMSensorsComponent*        _sensorsComponent;
    APMTuningComponent*         _tuningComponent;
    ESP8266Component*           _esp8266Component;
    APMHeliComponent*           _heliComponent;
    APMFollowComponent*         _followComponent;

private slots:
    //void         _advancedChanged        (bool advanced);
private:
    QVariantList _components;

};
