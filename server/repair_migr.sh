#!/bin/bash
mvn flyway:repair     -Dflyway.url=jdbc:mysql://127.0.0.1:3306/webchat     -Dflyway.user=webchatadmin     -Dflyway.password=password
mvn flyway:migrate     -Dflyway.url=jdbc:mysql://127.0.0.1:3306/webchat     -Dflyway.user=webchatadmin     -Dflyway.password=password
