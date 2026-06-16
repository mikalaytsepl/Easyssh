# Model Bazy Danych

Baza danych jest realizowana lokalnie przy użyciu **Room (SQLite)**. Aktualna wersja schematu: **8**
(`@Database(version = 8)` w `AppDatabase`). Migracje używają `fallbackToDestructiveMigration()`,
więc podbicie numeru wersji kasuje dotychczasowe dane.

Encje (tabele): `Server` (`servers`), `SshKey` (`ssh_keys`), `Snippet` (`snippets`).

## Serwery (`servers`)

| **Kolumna**           | **Typ Danych** | **Atrybuty**                | **Opis**                                                                 |
| --------------------- | -------------- | --------------------------- | ------------------------------------------------------------------------ |
| **`id`**              | `INT`          | **PK**, autoinkrementacja   | Unikalny identyfikator serwera.                                          |
| **`name`**            | `TEXT`         | `NOT NULL`                  | Przyjazna nazwa maszyny (np. _Prod-Web_).                                |
| **`ip`**              | `TEXT`         | `NOT NULL`                  | Adres IP / host serwera.                                                 |
| **`port`**            | `INT`          | `NOT NULL`, `DEFAULT 22`    | Port SSH używany do połączenia (domyślnie 22).                          |
| **`username`**        | `TEXT`         | `NOT NULL`                  | Nazwa użytkownika używana do logowania (np. _root_).                     |
| **`environment`**     | `TEXT`         | `NOT NULL`                  | Środowisko uruchomieniowe (np. _Production_, _Staging_).                |
| **`distro`**          | `TEXT`         | `NOT NULL`, `DEFAULT linux` | Dystrybucja systemu (ubuntu/debian/centos/...) — używana do ikony serwera. |
| **`keyId`**           | `INT`          | **FK**, `NULLABLE`          | Klucz obcy wskazujący na `ssh_keys.id` — domyślny klucz serwera.        |
| **`lastConnectedAt`** | `INTEGER`      | `NOT NULL`, `DEFAULT 0`     | Znacznik czasu ostatniego połączenia (epoch ms) — sekcja "Ostatnio używane" na Dashboardzie. |

## Klucze SSH (`ssh_keys`)

| **Kolumna**       | **Typ Danych** | **Atrybuty**       | **Opis**                                                        |
| ----------------- | -------------- | ------------------ | --------------------------------------------------------------- |
| **`id`**          | `INT`          | **PK**, autoinkrementacja | Unikalny identyfikator klucza SSH.                       |
| **`name`**        | `TEXT`         | `NOT NULL`         | Własna nazwa klucza (np. _DeployKey-Main_).                     |
| **`keyType`**     | `TEXT`         | `NOT NULL`         | Typ algorytmu (np. `"RSA"`, `"ED25519"`).                       |
| **`privateKey`**  | `TEXT`         | `NOT NULL`         | Zawartość klucza prywatnego (przechowywana w formie zaszyfrowanej). |
| **`publicKey`**   | `TEXT`         | `NULLABLE`         | Zawartość klucza publicznego.                                   |
| **`serverId`**    | `INT`          | **FK**, `NULLABLE` | Klucz obcy wskazujący na `servers.id` — serwer powiązany z kluczem. |

## Snippety (`snippets`)

| **Kolumna**    | **Typ Danych** | **Atrybuty**       | **Opis**                                                   |
| -------------- | -------------- | ------------------ | ---------------------------------------------------------- |
| **`id`**       | `INT`          | **PK**, autoinkrementacja | Unikalny identyfikator snippeta.                    |
| **`title`**    | `TEXT`         | `NOT NULL`         | Krótki tytuł opisujący akcję (np. _"Restart Nginx"_).      |
| **`category`** | `TEXT`         | `NOT NULL`         | Kategoria ułatwiająca filtrowanie (np. _Web_, _Database_). |
| **`command`**  | `TEXT`         | `NOT NULL`         | Pełna komenda bash/shell do wykonania.                     |

## Relacje w bazie

Pomiędzy `Server` a `SshKey` istnieje **relacja dwukierunkowa** realizująca koncepcję
"domyślnego klucza serwera":

- **`Server.keyId` → `SshKey.id`** — wskazuje domyślny klucz przypisany do serwera.
  Pole jest `NULLABLE` (serwer może nie mieć przypisanego klucza, np. logowanie hasłem).
  Operacje: `setDefaultKey(serverId, keyId)`, a przy usuwaniu klucza
  `clearDefaultKeyEverywhere(keyId)` zeruje to powiązanie we wszystkich serwerach.
- **`SshKey.serverId` → `Server.id`** — wskazuje serwer powiązany z danym kluczem.
  Pole jest `NULLABLE`. Operacja: `setKeyServer(keyId, serverId)`.

`Snippet` nie posiada powiązań (kluczy obcych) z pozostałymi tabelami.

> Uwaga: Room nie wymusza ograniczeń kluczy obcych (`foreign keys`) na poziomie SQLite
> dla tych encji — relacje są utrzymywane na poziomie logiki aplikacji (DAO/repozytoria).
