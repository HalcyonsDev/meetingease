-- author: halcyon
-- changeset: insert-data

-- insertData: deals
INSERT INTO deals (type, required_documents)
VALUES ('Кредитование', '["Удостоверение личности (паспорт)", "Справка о доходах или зарплатная ведомость", "Справка о трудоустройстве"]'),
       ('Открытие банковского счёта', '["Удостоверение личности (паспорт)", "Свидетельство о регистрации налогоплательщика (ИНН)", "Документы, подтверждающие место жительства"]'),
       ('Оформление аренды', '["Удостоверение личности (паспорт)", "Документы о доходах", "Справка о трудоустройстве"]');