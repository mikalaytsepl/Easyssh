# Model Bazy Danych
## Serwery

| **Kolumna**       | **Typ Danych** | **Atrybuty**       | **Opis**                                                 |
| ----------------- | -------------- | ------------------ | -------------------------------------------------------- |
| **`id`**          | `UUID` / `INT` | **PK**, `NOT NULL` | Unikalny identyfikator serwera.                          |
| **`name`**        | `VARCHAR(255)` | `NOT NULL`         | Przyjazna nazwa maszyny (np. _Prod-Web_).                |
| **`ip`**          | `VARCHAR(45)`  | `NOT NULL`         | Adres IP serwera (obsługuje IPv4 oraz IPv6).             |
| **`port`**        | `INT`          | `DEFAULT 22`       | Port SSH używany do połączenia (domyślnie 22).           |
| **`username`**    | `VARCHAR(100)` | `NOT NULL`         | Nazwa użytkownika używana do logowania (np. _root_).     |
| **`key_id`**      | `UUID` / `INT` | **FK**, `NULLABLE` | Klucz obcy wskazujący na tabelę `SshKey`.                |
| **`environment`** | `VARCHAR(50)`  | `NOT NULL`         | Środowisko uruchomieniowe (np. _Production_, _Staging_). |
| `shelltype`       | `VARCHAR(15)`  | `DEFAULT BASH`     | Typ powłoki używanej na serwerze                         |
## Ssh Key
| **Kolumna**       | **Typ Danych** | **Atrybuty**       | **Opis**                                              |
| ----------------- | -------------- | ------------------ | ----------------------------------------------------- |
| **`id`**          | `UUID` / `INT` | **PK**, `NOT NULL` | Unikalny identyfikator klucza SSH.                    |
| **`name`**        | `VARCHAR(255)` | `NOT NULL`         | Własna nazwa klucza (np. _DeployKey-Main_).           |
| **`key_type`**    | `ENUM`         | `NOT NULL`         | Typ algorytmu (wartości: `'RSA'`, `'Ed25519'`, etc.). |
| **`key_content`** | `TEXT`         | `NOT NULL`         | Właściwa zawartość klucza prywatnego/publicznego.     |
## Snippet
| **Kolumna**    | **Typ Danych** | **Atrybuty**       | **Opis**                                                   |
| -------------- | -------------- | ------------------ | ---------------------------------------------------------- |
| **`id`**       | `UUID` / `INT` | **PK**, `NOT NULL` | Unikalny identyfikator snippeta.                           |
| **`title`**    | `VARCHAR(255)` | `NOT NULL`         | Krótki tytuł opisujący akcję (np. _"Restart Nginx"_).      |
| **`command`**  | `TEXT`         | `NOT NULL`         | Pełna komenda bash/shell do wykonania.                     |
| **`category`** | `VARCHAR(100)` | `NULLABLE`         | Kategoria ułatwiająca filtrowanie (np. _Web_, _Database_). |
## Relacje w bazie
**`Server.key_id` → `SshKey.id`**: Relacja typu _Wiele-do-Jednego_ (Many-to-One). Wiele serwerów może korzystać z tego samego klucza SSH do logowania. Jest to klucz obcy (Foreign Key), który w przypadku braku powiązanego klucza może przyjmować wartość `NULL` (jeśli dopuszczasz logowanie na hasło, choć dla SSH zalecane są klucze).
