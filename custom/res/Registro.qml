/****************************************************************************
 *
 * (c) 2009-2020 QGROUNDCONTROL PROJECT <http://www.qgroundcontrol.org>
 *
 * QGroundControl is licensed according to the terms in the file
 * COPYING.md in the root of the source code directory.
 *
 ****************************************************************************/


import QtQuick                  2.3
import QtQuick.Controls         1.2
import QtQuick.Controls.Styles  1.4
import QtQuick.Dialogs          1.2
import QtQuick.Layouts          1.2

import QGroundControl                       1.0
import QGroundControl.FactSystem            1.0
import QGroundControl.FactControls          1.0
import QGroundControl.Controls              1.0
import QGroundControl.ScreenTools           1.0
import QGroundControl.MultiVehicleManager   1.0
import QGroundControl.Palette               1.0
import QGroundControl.Controllers           1.0
import QGroundControl.SettingsManager       1.0

Rectangle {
    id:                 _root
    color:              qgcPal.window
    anchors.fill:       parent
    anchors.margins:    ScreenTools.defaultFontPixelWidth

    QGCFlickable {
        clip:               true
        anchors.fill:       parent
        contentHeight:      outerItem.height
        contentWidth:       outerItem.width

        Item {
            id:     outerItem
            width:  Math.max(_root.width, settingsColumn.width)
            height: settingsColumn.height

            ColumnLayout {
                id:                         settingsColumn
                anchors.horizontalCenter:   parent.horizontalCenter

                QGCLabel {
                    id:                     nomeLabel
                    Layout.alignment:       Qt.AlignVCenter
                    color:                  qgcPal.text
                    text:                   "Nome"
                    visible:                true
                }
                QGCTextField {
                    id:                     nomeInput
                    text:                   "Digite seu nome"
                    enabled:                true
                    inputMethodHints:       Qt.ImhNone
                }

                QGCLabel {
                    id:                     passwordLabel
                    Layout.alignment:       Qt.AlignVCenter
                    color:                  qgcPal.text
                    text:                   "Senha"
                    visible:                true
                }
                QGCTextField {
                    id:                     passwordInput
                    text:                   "Digite sua senha"
                    enabled:                true
                    inputMethodHints:       Qt.ImhHiddenText
                }

                QGCLabel {
                    id:                     emailLabel
                    Layout.alignment:       Qt.AlignVCenter
                    color:                  qgcPal.text
                    text:                   "Email"
                    visible:                true
                }
                QGCTextField {
                    id:                     emailInput
                    text:                   "Digite seu email"
                    enabled:                true
                    inputMethodHints:       Qt.ImhNone
                }

                QGCLabel {
                    id:                     phoneLabel
                    Layout.alignment:       Qt.AlignVCenter
                    color:                  qgcPal.text
                    text:                   "Telefone"
                    visible:                true
                }
                QGCTextField {
                    id:             phoneInput
                    text:           "Digite seu telefone"
                    enabled:        true
                    inputMethodHints:    Qt.ImhNone
                    onEditingFinished: {
                        //registro.setPhone();
                    }
                }

                QGCButton {
                    width:                  height
                    height:                 baseFontEdit.height * 1.5
                    text:                   "Registrar"
                    onClicked: {
                        registro.doSomething();
                    }
                }
      }
     }
    }

}
