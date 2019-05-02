# IssueRobot #
Консольное приложение для продвинутого назначения исполнителей на задачи.
Задачей приложения является автоматизация процесса назначения задач на исполнителей с минимальным количеством ошибочных назначений.

Приложение использует: [jira-client](https://github.com/rcarz/jira-client), [launch4j-maven-plugin](https://github.com/lukaszlenart/launch4j-maven-plugin), [tomita-parser](https://github.com/yandex/tomita-parser)

## Фичи ##
На текущий момент функционал приложения позволяет по результату jql-запроса
* Добавить комментарий
* Добавить одну или несколько меток (label)
* Изменить одно или несколько пользовательских полей (custumer_field)
* Назначать поочередно исполнителей из списка. Можно использовать в проектах, где необходимо по-ровну разделить поступающие задачи между исполнителями.
* Определить наиболее вероятное направление задачи и назначить следующего по очереди исполнителя из этого направления. Можно использовать в проектах, где исполнители специализируются на решении узкого круга задач, но все задачи решаются в рамках одного проекта. 
Для примера подойдет направление технической поддержки в компании, предоставляющей большое количество разнообразных сервисов с большим количеством поступающих задач. Приложение позволяет освободить сотрудников от необходимости самим находить свои задачи в общей куче либо освободить сотрудников от роли координаторов если такие имеются. Для реализации этой части функционала используется парсер естественного языка Tomita-parser, на текущий момент поддерживающий морфологию только русского и украинского языков.
* Изменять пользовательские поля для этих направлений
