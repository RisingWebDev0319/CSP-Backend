package ca.freshstart.types;

public interface Constants {
    String AUTH_HEADER_NAME = "AuthKey";// название авторизационного заголовка
    String DATE_FORMAT = "MM-dd-yyyy";// формат дат в системе
    String DATE_FORMAT_REMOTE = "MM-dd-yyyy";// формат дат в удалённом сервере
    String DATE_FORMAT_URL_REMOTE = "MM/dd/yyyy";// формат дат в URL к удалённому серверу
    String TIME_FORMAT = "HH:mm";// формат времени в системе
    String TIME_FORMAT_REMOTE = "hh:mm a";// формат времени в системе
    long SUPER_USER_ID = 1;// ID супер пользователя
    long MILLISECONDS_PER_DAY = 24 * 60 * 60 * 1000;
}