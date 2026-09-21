# Спринт 3. Spring Framework
# Проектная работа: Бэкенд приложения-блога

Java 21, Spring Framework 6.2.7 (без Spring Boot), Spring MVC, JDBC, H2 и Maven.
Приложение собирается в WAR и работает в Tomcat 10.1 на `http://localhost:8080`.

## Открытие и проверка в IntelliJ IDEA

1. Выберите **Project SDK = JDK 21**; в настройках Maven Runner также выберите JDK 21.
2. Docker нужен только для готового фронтенда.
3. Запустите сохранённую конфигурацию **Blog — verify** либо выполните в терминале:
   ```powershell
   .\mvnw.cmd clean verify
   ```
4. Для запуска выберите **Blog — Tomcat** либо выполните:
   ```powershell
   .\mvnw.cmd package cargo:run
   ```
   Maven загрузит Tomcat и развернёт `target/ROOT.war` в корневой контекст.


5. Остановка: `Ctrl+C` в терминале запуска или `.\mvnw.cmd cargo:stop`.
6. Для тестов сервер и Docker не требуются.