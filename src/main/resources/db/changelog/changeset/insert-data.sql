-- author: halcyon
-- changeset: insert-data

-- insertData: deals, agents
INSERT INTO deals (type, required_documents)
VALUES ('Кредитование', '["Удостоверение личности (паспорт)", "Справка о доходах или зарплатная ведомость", "Справка о трудоустройстве"]'),
       ('Открытие банковского счёта', '["Удостоверение личности (паспорт)", "Свидетельство о регистрации налогоплательщика (ИНН)", "Документы, подтверждающие место жительства"]'),
       ('Оформление аренды', '["Удостоверение личности (паспорт)", "Документы о доходах", "Справка о трудоустройстве"]');

INSERT INTO agents (name, surname, email, phone_number, city, password, photo)
VALUES ('Иван', 'Иванов', 'ivan.ivanov@example.com', '+1234567890', 'Казань', 'ExamplePassword123', 'avatar.jpg'),
       ('Анна', 'Петрова', 'ann.petrova@example.com', '+1987654321', 'Казань', 'ExamplePassword123', 'avatar.jpg'),
       ('Петр', 'Сидоров', 'petr.sidorov@example.com', '+198723421', 'Санкт-Петербург', 'ExamplePassword123', 'avatar.jpg'),
       ('Майкл', 'Смирнов', 'michael.smirnov@example.com', '+138121231', 'Екатеринбург', 'ExamplePassword123', 'avatar.jpg');