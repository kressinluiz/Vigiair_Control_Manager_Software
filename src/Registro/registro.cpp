#include "registro.h"

#include <QDebug>

Registro::Registro(QGCApplication* app, QGCToolbox* toolbox)
    : QGCTool(app, toolbox)
{

}

Registro::~Registro() {
    qDebug() << "Done";
}

void Registro::doSomething()
{
    QSqlDatabase db = QSqlDatabase::addDatabase("QMYSQL");
    db.setHostName("127.0.0.1");
    db.setUserName("root");
    db.setPassword("vigiair1612");
    db.setDatabaseName("qt5");

    if(db.open()){
        qDebug() << "Database Connected Successfuly";
    }else{
        qDebug() << "Database is not connected";
    }
}
