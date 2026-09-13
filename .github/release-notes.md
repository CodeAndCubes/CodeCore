## Что в выпуске

| Файл | Куда |
|---|---|
| `CodeCore-*-server.jar` | `mods/` сервера |
| `CodeCore-*-client.jar` | `mods/` клиента, обязателен: ядро не объявляет `acceptableRemoteVersions` |
| `codecore-*-api.jar` | тем, кто пишет свой мод или провайдер: компиляция, на сервер не нужен |
| `codecore-*-platform.jar` | им же, когда нужны хелперы с типами игры |
| `codecore-*-dev.jar` | им же: deobf-версия для dev-запусков |

Minecraft 1.7.10, Forge 10.13.4.1614. Java 8 или 17 и 21 под lwjgl3ify. Байткод восьмой, тесты
прогоняются на Java 8, 21 и 25.
