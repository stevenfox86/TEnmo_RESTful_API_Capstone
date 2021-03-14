BEGIN TRANSACTION;

DROP TABLE IF EXISTS transfers;
DROP TABLE IF EXISTS accounts;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS transfer_types;
DROP TABLE IF EXISTS transfer_statuses;

DROP SEQUENCE IF EXISTS seq_transfer_type_id;
DROP SEQUENCE IF EXISTS seq_transfer_status_id;
DROP SEQUENCE IF EXISTS seq_user_id;
DROP SEQUENCE IF EXISTS seq_account_id;
DROP SEQUENCE IF EXISTS seq_transfer_id;

CREATE SEQUENCE seq_transfer_type_id
  INCREMENT BY 1
  NO MAXVALUE
  NO MINVALUE
  CACHE 1;

CREATE SEQUENCE seq_transfer_status_id
  INCREMENT BY 1
  NO MAXVALUE
  NO MINVALUE
  CACHE 1;

CREATE SEQUENCE seq_user_id
  INCREMENT BY 1
  START WITH 1001
  NO MAXVALUE
  NO MINVALUE
  CACHE 1;

CREATE SEQUENCE seq_account_id
  INCREMENT BY 1
  START WITH 2001
  NO MAXVALUE
  NO MINVALUE
  CACHE 1;

CREATE SEQUENCE seq_transfer_id
  INCREMENT BY 1
  START WITH 3001
  NO MAXVALUE
  NO MINVALUE
  CACHE 1;


CREATE TABLE transfer_types (
	transfer_type_id int DEFAULT nextval('seq_transfer_type_id'::regclass) NOT NULL,
	transfer_type_desc varchar(10) NOT NULL,
	CONSTRAINT PK_transfer_types PRIMARY KEY (transfer_type_id)
);

CREATE TABLE transfer_statuses (
	transfer_status_id int DEFAULT nextval('seq_transfer_status_id'::regclass) NOT NULL,
	transfer_status_desc varchar(10) NOT NULL,
	CONSTRAINT PK_transfer_statuses PRIMARY KEY (transfer_status_id)
);

CREATE TABLE users (
	user_id int DEFAULT nextval('seq_user_id'::regclass) NOT NULL,
	username varchar(50) NOT NULL,
	password_hash varchar(200) NOT NULL,
	CONSTRAINT PK_user PRIMARY KEY (user_id),
	CONSTRAINT UQ_username UNIQUE (username)
);

CREATE TABLE accounts (
	account_id int DEFAULT nextval('seq_account_id'::regclass) NOT NULL,
	user_id int NOT NULL,
	balance decimal(13, 2) NOT NULL,
	CONSTRAINT PK_accounts PRIMARY KEY (account_id),
	CONSTRAINT FK_accounts_user FOREIGN KEY (user_id) REFERENCES users (user_id)
);

CREATE TABLE transfers (
	transfer_id int DEFAULT nextval('seq_transfer_id'::regclass) NOT NULL,
	transfer_type_id int NOT NULL,
	transfer_status_id int NOT NULL,
	account_from int NOT NULL,
	account_to int NOT NULL,
	amount decimal(13, 2) NOT NULL,
	CONSTRAINT PK_transfers PRIMARY KEY (transfer_id),
	CONSTRAINT FK_transfers_accounts_from FOREIGN KEY (account_from) REFERENCES accounts (account_id),
	CONSTRAINT FK_transfers_accounts_to FOREIGN KEY (account_to) REFERENCES accounts (account_id),
	CONSTRAINT FK_transfers_transfer_statuses FOREIGN KEY (transfer_status_id) REFERENCES transfer_statuses (transfer_status_id),
	CONSTRAINT FK_transfers_transfer_types FOREIGN KEY (transfer_type_id) REFERENCES transfer_types (transfer_type_id),
	CONSTRAINT CK_transfers_not_same_account CHECK  ((account_from<>account_to)),
	CONSTRAINT CK_transfers_amount_gt_0 CHECK ((amount>0))
);


INSERT INTO transfer_statuses (transfer_status_desc) VALUES ('Pending');
INSERT INTO transfer_statuses (transfer_status_desc) VALUES ('Approved');
INSERT INTO transfer_statuses (transfer_status_desc) VALUES ('Rejected');

INSERT INTO transfer_types (transfer_type_desc) VALUES ('Request');
INSERT INTO transfer_types (transfer_type_desc) VALUES ('Send');

COMMIT TRANSACTION;




INSERT INTO users (username, password_hash)
VALUES ('jasmine', 'snhiefgev');
INSERT INTO users (username, password_hash)
VALUES ('steven', 'snhiefgev');
INSERT INTO users (username, password_hash)
VALUES ('james', 'snhiefgev');
INSERT INTO users (username, password_hash)
VALUES ('larry', 'snhiefgev');
INSERT INTO users (username, password_hash)
VALUES ('bernice', 'snhiefgev');
INSERT INTO users (username, password_hash)
VALUES ('joe', 'snhiefgev');

INSERT INTO accounts (user_id, balance)
VALUES (1001, 1000);
INSERT INTO accounts (user_id, balance)
VALUES (1002, 1000);
INSERT INTO accounts (user_id, balance)
VALUES (1003, 1000);
INSERT INTO accounts (user_id, balance)
VALUES (1004, 1000);
INSERT INTO accounts (user_id, balance)
VALUES (1005, 1000);
INSERT INTO accounts (user_id, balance)
VALUES (1006, 1000);


-- (3) SEE MY CURRENT ACCOUNT BALANCE
SELECT balance FROM accounts WHERE user_id = 1001;

--(4.1) LIST OF USERS TO SEND TEBUCKS TO
SELECT user_id AS ID, username AS Name FROM users WHERE username NOT LIKE '%jasmine%';

SELECT * FROM transfers;

--(4.2 INCLUDES 4.6) MAKE A TRANSFER

INSERT INTO transfers(transfer_type_id, transfer_status_id, account_from, account_to, amount)
VALUES(2, 2, 
(SELECT account_id FROM accounts WHERE user_id = (SELECT user_id FROM users WHERE username = 'jasmine')), 
(SELECT account_id FROM accounts WHERE user_id = (SELECT user_id FROM users WHERE username = 'steven')),
1000); 














INSERT INTO transfers(transfer_type_id, transfer_status_id, account_from, account_to, amount)
VALUES(2, 2, 
(SELECT account_id FROM accounts WHERE user_id = (SELECT user_id FROM users WHERE username = 'steven')), 
(SELECT account_id FROM accounts WHERE user_id = (SELECT user_id FROM users WHERE username = 'jasmine')),
1000);

SELECT * FROM accounts;

--(4.3 + 4.4) UPDATING ACCOUNT BALANCES
UPDATE accounts
SET balance = 2000
WHERE user_id = 1001;

update accounts
SET balance = 0  
WHERE user_id = 1002; 


SELECT * FROM accounts;
-- until here --

--SELECT user_id, balance FROM accounts WHERE user_id = 1001;
--SELECT user_id, balance FROM accounts WHERE user_id = 1002;

--(5) SELECT ALL 'MY' TRANSFERS - FROM AND TO 'ME'

SELECT t.transfer_id AS ID, u.username AS From_To, t.amount AS Amount
FROM transfers AS t 
INNER JOIN accounts AS a ON a.account_id = t.account_to OR 
                            a.account_id = t.account_from                   
INNER JOIN users AS u ON u.user_id = a.user_id; 

--WHERE u.username NOT LIKE '%jasmine%';

--SELECT t.transfer_id AS ID, u.username AS From_To, t.amount AS Amount
--FROM transfers AS t INNER JOIN accounts AS a ON a.account_id = t.account_from
--INNER JOIN users AS u ON u.user_id = a.user_id WHERE u.username NOT LIKE '%jasmine%';


--(6) SELECT ANY TRANSFER BY ID
--SELECT transfer_id, account_from, account_to, transfer_type_id, transfer_status_id, amount FROM transfers WHERE transfer_id = 3001;
SELECT t.transfer_id AS ID, 
        
        (SELECT username FROM users WHERE user_id = (SELECT user_id FROM accounts WHERE account_id = (SELECT account_from FROM transfers WHERE transfer_id = 3001))) AS From, 
        
        (SELECT username FROM users WHERE user_id = (SELECT user_id FROM accounts WHERE account_id = (SELECT account_to FROM transfers WHERE transfer_id = 3001))) AS To, 
        
        tt.transfer_type_desc AS Type, ts.transfer_status_desc AS Status, t.amount AS Amount

FROM transfer_types AS tt
        INNER JOIN transfers AS t
                ON t.transfer_type_id = tt.transfer_type_id
                INNER JOIN accounts AS a
                        ON a.account_id = t.account_from
                        INNER JOIN users AS u
                                ON u.user_id = a.user_id
                                INNER JOIN transfer_statuses AS ts
                                        ON ts.transfer_status_id = t.transfer_status_id
WHERE u.username NOT LIKE '%jasmine%';
