-- Change type in event and service
ALTER TABLE event ALTER COLUMN price type float;
ALTER TABLE service ALTER COLUMN price type float;
ALTER TABLE event ALTER COLUMN price_purchase type float;
ALTER TABLE service ALTER COLUMN price_purchase type float;