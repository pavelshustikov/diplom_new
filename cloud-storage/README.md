Облачное хранилище (Backend)
Это бэкенд-часть дипломного проекта, представляющая собой RESTful веб-сервис для хранения файлов. Сервис предоставляет пользователям возможность регистрироваться, входить в систему, загружать свои файлы, просматривать их список, скачивать, переименовывать и удалять.

Основные возможности
Аутентификация: Регистрация и вход пользователей с использованием JWT (JSON Web Token).

Управление файлами:
Загрузка файлов на сервер.
Получение списка загруженных файлов.
Скачивание файлов.
Переименование файлов.
Удаление файлов.
Безопасность: Все операции с файлами требуют авторизации по токену.
Управление схемой БД: Использование Liquibase для версионирования и применения изменений схемы базы данных.

Технологический стек
Java 17: Основной язык программирования.
Spring Boot: Каркас для создания приложения.
Spring Web: Для создания REST-контроллеров.
Spring Data JPA: Для работы с базой данных.
Spring Security: Для обеспечения безопасности и аутентификации.
H2 Database: Встраиваемая база данных для разработки и тестирования.
Liquibase: Система управления миграциями схемы базы данных.
Gradle: Система автоматической сборки проекта.
Lombok: Библиотека для сокращения шаблонного кода.

Запуск проекта:

1. Клонирование репозитория
git clone https://github.com/pavelshustikov/diplom_new.git
cd diplom_new/cloud-storage

2. Конфигурация приложения
В проекте, в директории src/main/resources/, создайте файл application.properties. Скопируйте в него следующие настройки и замените значения JWT ключа на свои:
# Server port
server.port=8080
# H2 Database Settings
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
spring.datasource.url=jdbc:h2:mem:cloud_storage;DB_CLOSE_DELAY=-1
spring.datasource.username=sa
spring.datasource.password=password
spring.datasource.driver-class-name=org.h2.Driver
# JPA/Hibernate settings
spring.jpa.hibernate.ddl-auto=none # Используем Liquibase для управления схемой
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect
# Liquibase settings
spring.liquibase.change-log=classpath:db/changelog/db.changelog-master.yaml
# JWT settings
jwt.secret=ВАШ_СУПЕР_СЕКРЕТНЫЙ_КЛЮЧ_ДЛЯ_JWT_ДОЛЖЕН_БЫТЬ_ДЛИННЫМ
jwt.lifetime=3600000 # Время жизни токена в миллисекундах (1 час)
# File storage path
storage.location=./uploads # Директория для хранения загруженных файлов

3. Сборка и запуск
Сборка проекта
Выполните в корневой директории cloud-storage команду Gradle для сборки .jar файла:
gradle clean build

Запуск приложения
После успешной сборки запустите приложение:
java -jar target/cloud-storage-0.0.1-SNAPSHOT.jar
Сервер будет запущен и доступен по адресу http://localhost:8080. Веб-консоль базы данных H2 будет доступна по адресу http://localhost:8080/h2-console.