message("Adding Custom Plugin")

#-- Version control
#   Major and minor versions are defined here (manually)

CUSTOM_QGC_VER_MAJOR = 0
CUSTOM_QGC_VER_MINOR = 2
CUSTOM_QGC_VER_FIRST_BUILD = 0

# Build number is automatic
# Uses the current branch. This way it works on any branch including build-server's PR branches
CUSTOM_QGC_VER_BUILD = $$system(git --git-dir ../.git rev-list $$GIT_BRANCH --first-parent --count)
win32 {
    CUSTOM_QGC_VER_BUILD = $$system("set /a $$CUSTOM_QGC_VER_BUILD - $$CUSTOM_QGC_VER_FIRST_BUILD")
} else {
    CUSTOM_QGC_VER_BUILD = $$system("echo $(($$CUSTOM_QGC_VER_BUILD - $$CUSTOM_QGC_VER_FIRST_BUILD))")
}
CUSTOM_QGC_VERSION = $${CUSTOM_QGC_VER_MAJOR}.$${CUSTOM_QGC_VER_MINOR}.$${CUSTOM_QGC_VER_BUILD}

DEFINES -= GIT_VERSION=\"\\\"$$GIT_VERSION\\\"\"
DEFINES += GIT_VERSION=\"\\\"$$CUSTOM_QGC_VERSION\\\"\"

message(Custom QGC Version: $${CUSTOM_QGC_VERSION})

# Build a single flight stack by disabling APM support
#MAVLINK_CONF = common
#CONFIG  += QGC_DISABLE_PX4_MAVLINK
#CONFIG  += QGC_DISABLE_PX4_PLUGIN QGC_DISABLE_PX4_PLUGIN_FACTORY

# We implement our own PX4 plugin factory
CONFIG  += QGC_DISABLE_APM_PLUGIN
CONFIG += QGC_DISABLE_APM_PLUGIN_FACTORY
CONFIG  += QGC_DISABLE_PX4_PLUGIN_FACTORY

# Branding

DEFINES += CUSTOMHEADER=\"\\\"CustomPlugin.h\\\"\"
DEFINES += CUSTOMCLASS=CustomPlugin

TARGET   = VigiManager
DEFINES += QGC_APPLICATION_NAME=\"\\\"VigiManager\\\"\"

DEFINES += QGC_ORG_NAME=\"\\\"qgroundcontrol.org\\\"\"
DEFINES += QGC_ORG_DOMAIN=\"\\\"org.qgroundcontrol\\\"\"

QGC_APP_NAME        = "VigiManager"
QGC_BINARY_NAME     = "VigiManager"
QGC_ORG_NAME        = "Vigiair"
QGC_ORG_DOMAIN      = "org.qgroundcontrol"
QGC_APP_DESCRIPTION = "Gerenciador de Rotas Vigiair"
QGC_APP_COPYRIGHT   = "Copyright (C) 2021 Vigiair Development Team. All rights reserved."

# Our own, custom resources
RESOURCES += \
    $$PWD/src/FirmwarePlugin/APM/APMResources.qrc \
    $$QGCROOT/custom/custom.qrc

QML_IMPORT_PATH += \
    $$QGCROOT/custom/res

# Our own, custom sources
SOURCES += \
    $$PWD/src/AutoPilotPlugin/APM/APMAirframeComponent.cc \
    $$PWD/src/AutoPilotPlugin/APM/APMAirframeComponentController.cc \
    $$PWD/src/AutoPilotPlugin/APM/APMAutoPilotPlugin.cc \
    $$PWD/src/AutoPilotPlugin/APM/APMCameraComponent.cc \
    $$PWD/src/AutoPilotPlugin/APM/APMCompassCal.cc \
    $$PWD/src/AutoPilotPlugin/APM/APMFlightModesComponent.cc \
    $$PWD/src/AutoPilotPlugin/APM/APMFlightModesComponentController.cc \
    $$PWD/src/AutoPilotPlugin/APM/APMFollowComponent.cc \
    $$PWD/src/AutoPilotPlugin/APM/APMFollowComponentController.cc \
    $$PWD/src/AutoPilotPlugin/APM/APMHeliComponent.cc \
    $$PWD/src/AutoPilotPlugin/APM/APMLightsComponent.cc \
    $$PWD/src/AutoPilotPlugin/APM/APMMotorComponent.cc \
    $$PWD/src/AutoPilotPlugin/APM/APMPowerComponent.cc \
    $$PWD/src/AutoPilotPlugin/APM/APMRadioComponent.cc \
    $$PWD/src/AutoPilotPlugin/APM/APMSafetyComponent.cc \
    $$PWD/src/AutoPilotPlugin/APM/APMSensorsComponent.cc \
    $$PWD/src/AutoPilotPlugin/APM/APMSensorsComponentController.cc \
    $$PWD/src/AutoPilotPlugin/APM/APMSubFrameComponent.cc \
    $$PWD/src/AutoPilotPlugin/APM/APMSubMotorComponentController.cc \
    $$PWD/src/AutoPilotPlugin/APM/APMTuningComponent.cc \
    $$PWD/src/AutoPilotPlugin/APM/CustomClass.cc \
    $$PWD/src/CustomPlugin.cc \
    $$PWD/src/CustomQuickInterface.cc \
    $$PWD/src/CustomVideoManager.cc \
    $$PWD/src/FirmwarePlugin/APM/APMFirmwarePlugin.cc \
    $$PWD/src/FirmwarePlugin/APM/APMFirmwarePluginFactory.cc \
    $$PWD/src/FirmwarePlugin/APM/APMParameterMetaData.cc \
    $$PWD/src/FirmwarePlugin/APM/ArduCopterFirmwarePlugin.cc \
    $$PWD/src/FirmwarePlugin/APM/ArduPlaneFirmwarePlugin.cc \
    $$PWD/src/FirmwarePlugin/APM/ArduRoverFirmwarePlugin.cc \
    $$PWD/src/FirmwarePlugin/APM/ArduSubFirmwarePlugin.cc

HEADERS += \
    $$PWD/src/AutoPilotPlugin/APM/APMAirframeComponent.h \
    $$PWD/src/AutoPilotPlugin/APM/APMAirframeComponentController.h \
    $$PWD/src/AutoPilotPlugin/APM/APMAutoPilotPlugin.h \
    $$PWD/src/AutoPilotPlugin/APM/APMCameraComponent.h \
    $$PWD/src/AutoPilotPlugin/APM/APMCompassCal.h \
    $$PWD/src/AutoPilotPlugin/APM/APMFlightModesComponent.h \
    $$PWD/src/AutoPilotPlugin/APM/APMFlightModesComponentController.h \
    $$PWD/src/AutoPilotPlugin/APM/APMFollowComponent.h \
    $$PWD/src/AutoPilotPlugin/APM/APMFollowComponentController.h \
    $$PWD/src/AutoPilotPlugin/APM/APMHeliComponent.h \
    $$PWD/src/AutoPilotPlugin/APM/APMLightsComponent.h \
    $$PWD/src/AutoPilotPlugin/APM/APMMotorComponent.h \
    $$PWD/src/AutoPilotPlugin/APM/APMPowerComponent.h \
    $$PWD/src/AutoPilotPlugin/APM/APMRadioComponent.h \
    $$PWD/src/AutoPilotPlugin/APM/APMSafetyComponent.h \
    $$PWD/src/AutoPilotPlugin/APM/APMSensorsComponent.h \
    $$PWD/src/AutoPilotPlugin/APM/APMSensorsComponentController.h \
    $$PWD/src/AutoPilotPlugin/APM/APMSubFrameComponent.h \
    $$PWD/src/AutoPilotPlugin/APM/APMSubMotorComponentController.h \
    $$PWD/src/AutoPilotPlugin/APM/APMTuningComponent.h \
    $$PWD/src/AutoPilotPlugin/APM/CustomClass.h \
    $$PWD/src/CustomPlugin.h \
    $$PWD/src/CustomQuickInterface.h \
    $$PWD/src/CustomVideoManager.h \
    $$PWD/src/FirmwarePlugin/APM/APMFirmwarePlugin.h \
    $$PWD/src/FirmwarePlugin/APM/APMFirmwarePluginFactory.h \
    $$PWD/src/FirmwarePlugin/APM/APMParameterMetaData.h \
    $$PWD/src/FirmwarePlugin/APM/ArduCopterFirmwarePlugin.h \
    $$PWD/src/FirmwarePlugin/APM/ArduPlaneFirmwarePlugin.h \
    $$PWD/src/FirmwarePlugin/APM/ArduRoverFirmwarePlugin.h \
    $$PWD/src/FirmwarePlugin/APM/ArduSubFirmwarePlugin.h

INCLUDEPATH += \
    $$PWD/src \

#-------------------------------------------------------------------------------------
# Custom Firmware/AutoPilot Plugin

INCLUDEPATH += \
    $$QGCROOT/custom/src/FirmwarePlugin \
    $$QGCROOT/custom/src/AutoPilotPlugin

HEADERS+= \
    $$QGCROOT/custom/src/AutoPilotPlugin/CustomAutoPilotPlugin.h \
    $$QGCROOT/custom/src/FirmwarePlugin/CustomCameraControl.h \
    $$QGCROOT/custom/src/FirmwarePlugin/CustomCameraManager.h \
    $$QGCROOT/custom/src/FirmwarePlugin/CustomFirmwarePlugin.h \
    $$QGCROOT/custom/src/FirmwarePlugin/CustomFirmwarePluginFactory.h \

SOURCES += \
    $$QGCROOT/custom/src/AutoPilotPlugin/CustomAutoPilotPlugin.cc \
    $$QGCROOT/custom/src/FirmwarePlugin/CustomCameraControl.cc \
    $$QGCROOT/custom/src/FirmwarePlugin/CustomCameraManager.cc \
    $$QGCROOT/custom/src/FirmwarePlugin/CustomFirmwarePlugin.cc \
    $$QGCROOT/custom/src/FirmwarePlugin/CustomFirmwarePluginFactory.cc \

DISTFILES += \
    $$PWD/qgcresources.exclusion \
    $$PWD/src/AutoPilotPlugin/APM/APMAirframeComponent.qml \
    $$PWD/src/AutoPilotPlugin/APM/APMAirframeComponentSummary.qml \
    $$PWD/src/AutoPilotPlugin/APM/APMCameraComponent.qml \
    $$PWD/src/AutoPilotPlugin/APM/APMCameraComponentSummary.qml \
    $$PWD/src/AutoPilotPlugin/APM/APMCameraSubComponent.qml \
    $$PWD/src/AutoPilotPlugin/APM/APMFlightModesComponent.qml \
    $$PWD/src/AutoPilotPlugin/APM/APMFlightModesComponentSummary.qml \
    $$PWD/src/AutoPilotPlugin/APM/APMFollowComponent.FactMetaData.json \
    $$PWD/src/AutoPilotPlugin/APM/APMFollowComponent.qml \
    $$PWD/src/AutoPilotPlugin/APM/APMFollowComponentSummary.qml \
    $$PWD/src/AutoPilotPlugin/APM/APMHeliComponent.qml \
    $$PWD/src/AutoPilotPlugin/APM/APMLightsComponent.qml \
    $$PWD/src/AutoPilotPlugin/APM/APMLightsComponentSummary.qml \
    $$PWD/src/AutoPilotPlugin/APM/APMMotorComponent.qml \
    $$PWD/src/AutoPilotPlugin/APM/APMNotSupported.qml \
    $$PWD/src/AutoPilotPlugin/APM/APMPowerComponent.qml \
    $$PWD/src/AutoPilotPlugin/APM/APMPowerComponentSummary.qml \
    $$PWD/src/AutoPilotPlugin/APM/APMRadioComponentSummary.qml \
    $$PWD/src/AutoPilotPlugin/APM/APMSafetyComponent.qml \
    $$PWD/src/AutoPilotPlugin/APM/APMSafetyComponentCopter.qml \
    $$PWD/src/AutoPilotPlugin/APM/APMSafetyComponentPlane.qml \
    $$PWD/src/AutoPilotPlugin/APM/APMSafetyComponentRover.qml \
    $$PWD/src/AutoPilotPlugin/APM/APMSafetyComponentSub.qml \
    $$PWD/src/AutoPilotPlugin/APM/APMSafetyComponentSummary.qml \
    $$PWD/src/AutoPilotPlugin/APM/APMSafetyComponentSummaryCopter.qml \
    $$PWD/src/AutoPilotPlugin/APM/APMSafetyComponentSummaryPlane.qml \
    $$PWD/src/AutoPilotPlugin/APM/APMSafetyComponentSummaryRover.qml \
    $$PWD/src/AutoPilotPlugin/APM/APMSafetyComponentSummarySub.qml \
    $$PWD/src/AutoPilotPlugin/APM/APMSensorsComponent.qml \
    $$PWD/src/AutoPilotPlugin/APM/APMSensorsComponentSummary.qml \
    $$PWD/src/AutoPilotPlugin/APM/APMSubFrameComponent.qml \
    $$PWD/src/AutoPilotPlugin/APM/APMSubFrameComponentSummary.qml \
    $$PWD/src/AutoPilotPlugin/APM/APMSubMotorComponent.qml \
    $$PWD/src/AutoPilotPlugin/APM/APMTuningComponentCopter.qml \
    $$PWD/src/AutoPilotPlugin/APM/APMTuningComponentSub.qml \
    $$PWD/src/AutoPilotPlugin/APM/CMakeLists.txt \
    $$PWD/src/AutoPilotPlugin/APM/Images/LightsComponentIcon.png \
    $$PWD/src/AutoPilotPlugin/APM/Images/SubFrameComponentIcon.png \
    $$PWD/src/AutoPilotPlugin/APM/Images/bluerov-frame.png \
    $$PWD/src/AutoPilotPlugin/APM/Images/simple3-frame.png \
    $$PWD/src/AutoPilotPlugin/APM/Images/simple4-frame.png \
    $$PWD/src/AutoPilotPlugin/APM/Images/simple5-frame.png \
    $$PWD/src/AutoPilotPlugin/APM/Images/vectored-frame.png \
    $$PWD/src/AutoPilotPlugin/APM/Images/vectored6dof-frame.png \
    $$PWD/src/FirmwarePlugin/APM/APMBrandImage.png \
    $$PWD/src/FirmwarePlugin/APM/APMBrandImageSub.png \
    $$PWD/src/FirmwarePlugin/APM/APMParameterFactMetaData.Copter.3.5.xml \
    $$PWD/src/FirmwarePlugin/APM/APMParameterFactMetaData.Copter.3.6.xml \
    $$PWD/src/FirmwarePlugin/APM/APMParameterFactMetaData.Copter.3.7.xml \
    $$PWD/src/FirmwarePlugin/APM/APMParameterFactMetaData.Copter.4.0.xml \
    $$PWD/src/FirmwarePlugin/APM/APMParameterFactMetaData.Plane.3.10.xml \
    $$PWD/src/FirmwarePlugin/APM/APMParameterFactMetaData.Plane.3.8.xml \
    $$PWD/src/FirmwarePlugin/APM/APMParameterFactMetaData.Plane.3.9.xml \
    $$PWD/src/FirmwarePlugin/APM/APMParameterFactMetaData.Plane.4.0.xml \
    $$PWD/src/FirmwarePlugin/APM/APMParameterFactMetaData.Rover.3.4.xml \
    $$PWD/src/FirmwarePlugin/APM/APMParameterFactMetaData.Rover.3.5.xml \
    $$PWD/src/FirmwarePlugin/APM/APMParameterFactMetaData.Rover.3.6.xml \
    $$PWD/src/FirmwarePlugin/APM/APMParameterFactMetaData.Rover.4.0.xml \
    $$PWD/src/FirmwarePlugin/APM/APMParameterFactMetaData.Sub.3.4.xml \
    $$PWD/src/FirmwarePlugin/APM/APMParameterFactMetaData.Sub.3.5.xml \
    $$PWD/src/FirmwarePlugin/APM/APMParameterFactMetaData.Sub.3.6.xml \
    $$PWD/src/FirmwarePlugin/APM/APMParameterFactMetaData.Sub.3.6dev.xml \
    $$PWD/src/FirmwarePlugin/APM/APMParameterFactMetaData.Sub.4.0.xml \
    $$PWD/src/FirmwarePlugin/APM/APMSensorParams.qml \
    $$PWD/src/FirmwarePlugin/APM/BuildParamMetaData.sh \
    $$PWD/src/FirmwarePlugin/APM/CMakeLists.txt \
    $$PWD/src/FirmwarePlugin/APM/Copter3.6.OfflineEditing.params \
    $$PWD/src/FirmwarePlugin/APM/MavCmdInfoCommon.json \
    $$PWD/src/FirmwarePlugin/APM/MavCmdInfoFixedWing.json \
    $$PWD/src/FirmwarePlugin/APM/MavCmdInfoMultiRotor.json \
    $$PWD/src/FirmwarePlugin/APM/MavCmdInfoRover.json \
    $$PWD/src/FirmwarePlugin/APM/MavCmdInfoSub.json \
    $$PWD/src/FirmwarePlugin/APM/MavCmdInfoVTOL.json \
    $$PWD/src/FirmwarePlugin/APM/Plane3.9.OfflineEditing.params \
    $$PWD/src/FirmwarePlugin/APM/QGroundControl.ArduPilot.qmldir \
    $$PWD/src/FirmwarePlugin/APM/Rover3.5.OfflineEditing.params

