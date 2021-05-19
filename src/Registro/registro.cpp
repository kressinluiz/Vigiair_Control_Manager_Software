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

        // Retrieve Data from Input Fields
//        QString username = ui->username->text();
//        QString password = ui->password->text();
//        QString email = ui->email->text();
//        QString phone = ui->phone->text();

//        // Run our insert query
//        QSqlQuery qry(QSqlDatabase::database("MyConnection"));
//        qry.prepare("INSERT INTO users (username, password, email, phone)"
//                    "VALUES (:username, :password, :email, :phone)");

//        qry.bindValue(":username", username);
//        qry.bindValue(":password", password);
//        qry.bindValue(":email", email);
//        qry.bindValue(":phone", phone);

//        if(qry.exec()) {
//            qDebug() << "Data inserted sucessfully";
//        } else {
//            qDebug() << "Data not inserted";
//        }
    }else{
        qDebug() << "Database is not connected";
    }
}
