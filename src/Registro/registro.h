#ifndef REGISTRO_H
#define REGISTRO_H

#include "QGCLoggingCategory.h"
#include "Joystick.h"
#include "MultiVehicleManager.h"
#include "QGCToolbox.h"

#include <QMainWindow>
#include <QtSql>
#include <QSqlDatabase>
#include <QMessageBox>

#include <QVariantList>

#include <QObject>

class Registro : public QGCTool
{
    Q_OBJECT
public:
    explicit Registro(QGCApplication* app, QGCToolbox* toolbox);
    ~Registro();

public slots:
    void doSomething();
};

#endif // REGISTRO_H
