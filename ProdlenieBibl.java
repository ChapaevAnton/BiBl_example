package com.W4ereT1ckRtB1tch.bibl.ui.prodlenie;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import com.W4ereT1ckRtB1tch.bibl.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;


public class ProdlenieBibl extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    //region переменные
    //временные переменные
    private String mBookId = null;//добавил чтобы передавать штрих код в метод добавления эелемента списка
    //список продления
    private RecyclerView mRecyclerView;
    private ArrayList<ExampleItem> mExampleList;
    private ExampleAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private TextView recyclerText, getbText;
    //кнопки
    private Button AutoScBtn, ProdBtn, backProdBtn;
    private MaterialToolbar topAppBar_prodlenie;
    //поля ввода
    private TextInputLayout DateBackEnter;
    private TextInputEditText DateBackEnterText;
    //Анимация переходов
    private ConstraintLayout main_rpodbibl_Layout;
    private Animation animeActivity = null; //переменная для хранения типа анимации, который был ранее создан в XML
    //диалоговые окна и уведомления
    private AlertDialog loadProdlenieDialog;
    private int ERR_MSG_TOAST;


    //камера
    private final int PERMISSION_REQUEST_CAMERA = 0;
    //база данных
    //параметры подключения к базе
    private final String SERVER;
    private final String LOGIN;
    private final String PASSWORD;
    //статусы и валиды
    private boolean isValidSetting, isValidConnect, isIsValidSavePrl, isIsIsValidSavePrlBook;

    {
        SERVER = "";
        LOGIN = "";
        PASSWORD = "";
        isValidSetting = false;
        isValidConnect = false;
        isIsValidSavePrl = false;
        isIsIsValidSavePrlBook = false;


    }

    //поток добавления
    private TaskTableProlongationInsert taskTableProlongationInsert;
    private TaskConnectStatus taskConnectStatus;
    //переменные считанных настроек для записи
    private String fIdUser, fMd5, fprlnGtnDate, fprlnGtn;
    private int fAutoIncKeyFromApi;
    //ключи доступа к записанным параметрам iduser и md5 и auth в файле настроек
    private final String ID_USER = "ID_USER";
    private final String ID_MD5 = "ID_MD5";
    //пул книг на продление
    private String[] bookPool;
    //список книг для продления что бы передавать в пул
    private ArrayList<String> itemsArray = new ArrayList<String>();


    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prodlenie_bibl);
        //элементы View
        setSrcItemView();
        //вызов метода создания списка книг в RecyclerView
        createExampleList();
        //создания RecyclerView
        buildRecyclerView();
        //кнопки
        setButton();
        setItemCount(mAdapter.getItemCount()); //количество элементов корзины
        //тексты
        setTxtView(); //предустановки - дата продления
        //проверяем наличие подключения к БД
        getStartConnect();

    }


    @Override
    protected void onStart() {
        //анимация при появлении активити
        super.onStart();
        strAni(); //анимация


    }


    //region предустановки
    //поле дата продления
    private void setTxtView() {
        DateBackEnterText.setText(getDatePrLng(14));
    }
    //endregion

    //region дата

    //метод возвращает рекомендованную дату продления
    private String getDatePrLng(int day) {
        String strDate;

        Calendar date = new GregorianCalendar();
        DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
        strDate = df.format(date.getTime());
        Log.d("DATE", strDate);
        date.add(Calendar.DAY_OF_MONTH, day);
        strDate = df.format(date.getTime());
        Log.d("DATE", strDate);
        return strDate;
    }

    //endregion


    //region элементы анимация
    private void strAni() {
        animeActivity = AnimationUtils.loadAnimation(ProdlenieBibl.this, R.anim.alpha_set);
        main_rpodbibl_Layout.startAnimation(animeActivity);
    }

    //элементы
    private void setSrcItemView() {
        topAppBar_prodlenie = (MaterialToolbar) findViewById(R.id.topAppBar_prodlenie);
        //для анимации
        main_rpodbibl_Layout = (ConstraintLayout) findViewById(R.id.main_rpodbibl_Layout);
        //элементы
        DateBackEnter = (TextInputLayout) findViewById(R.id.DateBackEnter);
        DateBackEnterText = (TextInputEditText) findViewById(R.id.DateBackEnterText);
        recyclerText = (TextView) findViewById(R.id.recyclerText);
        AutoScBtn = (Button) findViewById(R.id.AutoScBtn);
        ProdBtn = (Button) findViewById(R.id.ProdBtn);
        backProdBtn = (Button) findViewById(R.id.backProdBtn);
        getbText = (TextView) findViewById(R.id.getbText);

        //переменные
    }
    //endregion


    // region кнопки
    private void setButton() {
        //нажатие на кнопку сканирования - загатовка
        AutoScBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                acsCam();
            }
        });//конец нажатие на кнопку сканирования - загатовка


        //нажатие на кнопку продление
        ProdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getStartInsert();
            }
        });

        //нажатие кнопки назад на главной панеле
        topAppBar_prodlenie.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProdlenieBibl.this.finish();
            }
        }); //конец нажатия назад на главной панеле

        //нажатие на кнопку назад
        backProdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProdlenieBibl.this.finish();
            }
        });
        //нажатие на календарь
        DateBackEnter.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setDateCalendar(DateBackEnterText);
            }
        });

    }
    //endregion


    //region  пачка методов для списка
    //установка количества книг
    private void setItemCount(int count) {
        Log.d("SQL", "set items_count:" + count);
        getbText.setText(Integer.toString(count));
    }


    //метод добавления книги в список
    private void insertItem(int position, String bookId) {

        if (doubleItem(bookId)) { //если книга не дублирована то добавляем элемент
            mExampleList.add(position, new ExampleItem(bookId)); //сюда передавать значение
            mAdapter.notifyItemInserted(position); //обновление изменений в списке
            mLayoutManager.scrollToPosition(position); //плавная прокрутка вверх списка при добавлении нового эелемента
            //тут добавление в пулл продления
            insertPool(position, bookId);
            showErrToast(R.string.toast_prodlenie, R.color.ap_blue_2);
        } else
            showErrToast(R.string.toast_prodlenie3, R.color.err_toast);

        //размер списка
        Log.d("SQL", "itemsArray_count_insert:" + itemsArray.size());
        //выдаю в лог
        for (int i = 0; i < itemsArray.size(); i++) {
            Log.d("SQL", "itemsArray_insert[" + i + "]:" + itemsArray.get(i));
        }

    }

    //проверка на дублетность
    private boolean doubleItem(String bookId) {
        Log.d("SQL", "itemsArray_count_double:" + itemsArray.size());
        boolean bookDbl = true;
        if (!itemsArray.isEmpty()) {
            Log.d("SQL", "isEmpty");
            for (int i = 0; i < itemsArray.size(); i++) {

                if (itemsArray.get(i).equals(bookId)) {
                    bookDbl = false;
                    Log.d("SQL", "double book!!!");
                    break;
                } else bookDbl = true;

            }
        }
        return bookDbl;
    }


    //методо удаление книг из списка
    private void removeItem(int position) {
        mExampleList.remove(position);
        mAdapter.notifyItemRemoved(position);
        //тут убираем из пулла продления
        removePool(position);
        showErrToast(R.string.toast_prodlenie4, R.color.ap_blue_2);
        //размер списка
        Log.d("SQL", "itemsArray_count_remove:" + itemsArray.size());
        //выдаю в лог
        for (int i = 0; i < itemsArray.size(); i++) {
            Log.d("SQL", "itemsArray[" + i + "]_remove:" + itemsArray.get(i));
        }

    }

    //метод отчистки списка после продления
    private void clearItem() {
        if (!mExampleList.isEmpty()) {
            mExampleList.clear(); //очищаю список корзины
            clearPool(); //очищаю список книг
            mAdapter.notifyDataSetChanged(); //обновляю изменения
            Log.d("SQL", "mExampleList_count_clear:" + mExampleList.size());
        }
    }

    //метод заполнения пула книг
    private void insertPool(int position, String bookId) {
        //добавляю элемент в позицию
        itemsArray.add(position, bookId);
    }


    //метод изъятия из пула
    private void removePool(int position) {
        //убираю элемент позиции списка
        itemsArray.remove(position);
    }

    private void clearPool() {
        if (!itemsArray.isEmpty() & bookPool.length != 0) {
            itemsArray.clear();
            //размер списка
            Log.d("SQL", "itemsArray_count_clear:" + itemsArray.size());

        }
    }


    private void setPool() {
        //инициализирую массив
        bookPool = new String[itemsArray.size()];
        //передаю в масив список
        bookPool = itemsArray.toArray(bookPool);
        //размер списка
        Log.d("SQL", "itemsArray_count:" + itemsArray.size());
        //выдаю в лог
        for (int i = 0; i < itemsArray.size(); i++) {
            Log.d("SQL", "itemsArray[" + i + "]:" + itemsArray.get(i));
        }
        for (int i = 0; i < bookPool.length; i++) {
            Log.d("SQL", "bookpool[" + i + "]:" + bookPool[i]);
        }
    }

    //метод когда список пуст то убирать ресайкл и разворачиваем подсказку и деактивируем кнопку продлить
    private void hideRecycler(int itemCount) {
        if (itemCount == 0) {
            recyclerText.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
            ProdBtn.setEnabled(false);
        }
    }

    //если список не пуст то убирать подсказку и разворачивать ресайкл активируем кнопку продлить
    private void showRecycler(int itemCount) {
        if (itemCount != 0) {
            recyclerText.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
            ProdBtn.setEnabled(true);
        }
    }


    //метод создания списка первоначального
    private void createExampleList() {
        //список продления
        mExampleList = new ArrayList<>();
    }

    //метод создания RecyclerView и передача в него списка созданного в методе creatExampleList
    private void buildRecyclerView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mAdapter = new ExampleAdapter(mExampleList);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        //слушаю нажатие на кнопке удалить элемента
        mAdapter.setOnItemClickListener(new ExampleAdapter.OnItemClickListener() {
            @Override
            //удаляем эелемент
            public void onDeleteClick(int position) {
                removeItem(position);
                //если список пуст то убирать ресайкл и разворачиваем подсказку и деактивируем кнопку продлить
                hideRecycler(mAdapter.getItemCount());
                //уменьшить счетчик
                setItemCount(mAdapter.getItemCount());

            }
        });
    }
    //endregion конец пачка методов для списка

    //region диалоговые окна и уведомления


    //диалоговое окно загрузки старт
    private void startLoadProdDialog() {
        //параметры диалога
        AlertDialog.Builder builder = new AlertDialog.Builder(ProdlenieBibl.this);
        LayoutInflater inflater = ProdlenieBibl.this.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.custom_dialog_load, null));
        builder.setCancelable(false);

        //передаем и показываем
        loadProdlenieDialog = builder.create();
        loadProdlenieDialog.setCanceledOnTouchOutside(false); //отменяю закрытие при таче за границами
        loadProdlenieDialog.show();
    }

    //диалоговое окно загрузки стоп
    private void stopLoadProdDialog() {
        loadProdlenieDialog.dismiss();
    }

    //метод уведомления об ошибках пользователя
    private void showErrToast(int resStrId, int colorMsg) {
        Snackbar snackbarErrControl = Snackbar.make(findViewById(R.id.main_rpodbibl_Layout), resStrId, Snackbar.LENGTH_LONG);
        snackbarErrControl.setBackgroundTint(ContextCompat.getColor(this, colorMsg));
        snackbarErrControl.show();
    }

    //метод установки типа ошибки сообщения для пользователя.
    private void setErrMsgToast(int errMsg) {
        ERR_MSG_TOAST = errMsg;
    }


    //метод вызова календаря в поле
    private void setDateCalendar(TextInputEditText dateEnterText) {

        //переменные для диалогового окна даты
        Button celBtnReguser, okBtnReguser;
        Dialog customDialogDate;
        DatePicker datePicker;


        //элементы диалогового окна в main сам диалог
        customDialogDate = new Dialog(ProdlenieBibl.this);

        //dateEnterText.setText(null); //обнуляю поле ввода даты
        //создаю диалог из XML
        customDialogDate.setContentView(R.layout.custom_dialog_select_date);
        //находим элементы на диалоге
        celBtnReguser = (Button) customDialogDate.findViewById(R.id.celBtnReguser);
        okBtnReguser = (Button) customDialogDate.findViewById(R.id.okBtnReguser);
        datePicker = (DatePicker) customDialogDate.findViewById(R.id.datePicker_dialog_reguser);
        //добавляю слушатели кнопок на диалоге
        //кнопка отмены
        final Dialog fCustomDialogDate = customDialogDate;
        celBtnReguser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fCustomDialogDate.dismiss(); //просто закрываю диалог
            }
        });

        //кнопка OK
        final DatePicker fDatePicker = datePicker;
        final TextInputEditText fDateEnterText = dateEnterText;
        okBtnReguser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //задаю перменным день месяц год методами датапикера
                int day = fDatePicker.getDayOfMonth();
                int mon = fDatePicker.getMonth();
                int year = fDatePicker.getYear();
                //в переменную записываю дату строкой
                String outDate = String.format("%02d", day) + "." + String.format("%02d", mon + 1) + "." + year;
                fDateEnterText.setText(outDate); //передаю значение в поле
                fCustomDialogDate.dismiss();//закрываю диалог
            }
        });
        //так как использую бекграунд, задаю дефолтный бекграунд прозрачным
        customDialogDate.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        //показываю готовы диалог
        customDialogDate.show();
    }
    //endregion

    //region пачка методов для камеры

    //проверка есть ли камера на устройстве
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // this device has a camera

            return true;
        } else {
            showErrToast(R.string.toast_prodlenie2, R.color.err_toast);
            // no camera on this device
            return false;
        }
    }


    //получение доступа к камере
    private void acsCam() {

        //если камера есть на устройстве // this device has a camera
        if (checkCameraHardware(this)) {

            // Check if the Camera permission has been granted
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {


                // Permission is already available, start camera preview
                iniCamShow();
                Log.d("CAMERA", "access permission ok!!!");

            } else {
                // Permission is missing and must be requested.
                Log.d("CAMERA", "access permission?");
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        PERMISSION_REQUEST_CAMERA);

            }

        } else {
            // no camera on this device
        }
    }

    //иницилизация камеры
    private void iniCamShow() {
        IntentIntegrator intentIntegrator = new IntentIntegrator(ProdlenieBibl.this);
        intentIntegrator.setCaptureActivity(CamBarcode.class);
        intentIntegrator.setOrientationLocked(true);
        intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        intentIntegrator.setPrompt(getString(R.string.camessage));
        intentIntegrator.initiateScan();
    }


    //передаем значение камеры
    private void getResultCam(String getResult) {
        mBookId = getResult;
        int position = 0;
        //если не пустое значение то передать его
        if (mBookId != null && !mBookId.isEmpty()) {
            //добавляем в корзину
            insertItem(position, mBookId);
            //если список не пуст то убирать подсказку и разворачивать ресайкл активируем кнопку продлить
            showRecycler(mAdapter.getItemCount());
            //увеличиваем счетчик
            setItemCount(mAdapter.getItemCount());


        }
    }

    //получение значения камеры
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {

                //Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show(); //если отменили считывание
            } else {

                //передача результата
                getResultCam(result.getContents());


            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        // BEGIN_INCLUDE(onRequestPermissionsResult)
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            // Request for camera permission.
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission has been granted. Start camera preview Activity.
                showErrToast(R.string.toast_prodlenie1, R.color.ap_blue_2);
                Log.d("CAMERA", "access permission ok!!!");
                iniCamShow();
            } else {
                // Permission request was denied.
                showErrToast(R.string.toast_prodlenie2, R.color.err_toast);
                Log.d("CAMERA", "access permission err!!!");
            }
        }
        // END_INCLUDE(onRequestPermissionsResult)
    }


    //endregion

    //region методы работы с базой данных

    //проверяем соединение при старте активити если что то не так уведомляем пользователя
    private void getStartConnect() {

        getSaveSettings();
        Log.d("SQL", fIdUser); //выдаю пока в лог
        Log.d("SQL", fMd5); //выдаю пока в лог

        if (isValidSetting & fIdUser != "" & fMd5 != "") {
            startTaskConnectStatus();
            Log.d("SQL", "connect status run!!!");
        } else {
            Log.d("SQL", "connect status abort!!!");
            showErrToast(R.string.toast_prodlenie9,R.color.err_toast);

        }
    }


    //читаем записываем настройки
    private void getSaveSettings() {
        fIdUser = "";
        fMd5 = "";
        isValidSetting = false;
        try {

            SharedPreferences settings;
            settings = getSharedPreferences("settings", MODE_PRIVATE);
            fIdUser = settings.getString(ID_USER, "");
            fMd5 = settings.getString(ID_MD5, "");
            Log.d("SQL", "load setting id md5 ok!!!");
            isValidSetting = true;
        } catch (ClassCastException e) {
            //если ошибка при чтении настроек из файла
            Log.d("SQL", "load setting id md5 err!!!");
            isValidSetting = false;
        }
    }


    //метод запуска потока, но только после того как успешно считались данные из файла настроек и что переменные не пустые
    private void getStartInsert() {

        getSaveSettings();
        Log.d("SQL", fIdUser); //выдаю пока в лог
        Log.d("SQL", fMd5); //выдаю пока в лог

        if (isValidConnect & isValidSetting & fIdUser != "" & fMd5 != "") {
            startLoadProdDialog();
            runTaskTableProlongationInsert();
            Log.d("SQL", "update run!!!");
        } else {
            Log.d("SQL", "update abort!!!");
            //если данные пустые
            showErrToast(R.string.toast_prodlenie9,R.color.err_toast);
        }
    }


    //подключение к базе
    private Connection getDBConnection() {
        Connection dbConnection = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Log.d("SQL", "driver ok");
        } catch (ClassNotFoundException e) {
            //если ошибка драйвера
            stopTaskTableProlongationInsert(R.string.toast_prodlenie6);
            Log.d("SQL", e.getMessage());

        }
        try {
            DriverManager.setLoginTimeout(10);
            dbConnection = DriverManager.getConnection(SERVER, LOGIN, PASSWORD);
            Log.d("SQL", "connect ok");
            return dbConnection;
        } catch (SQLException e) {
            //если нет интернет соиденения
            stopTaskTableProlongationInsert(R.string.toast_prodlenie10);
            Log.d("SQL", e.getMessage());
        }
        return dbConnection;
    }//подключение к базе


    //region методы проверки наличия подключения к базе перед стартом активити и актуальности ссесии

//метод проверки подключения
    private Connection connectStatus() {
        Connection dbConnectStatus = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Log.d("SQL", "driver ok");
        } catch (ClassNotFoundException e) {
            //если ошибка драйвера
            stopTaskConnectStatus(R.string.toast_prodlenie6);
            Log.d("SQL", e.getMessage());

        }
        try {
            DriverManager.setLoginTimeout(10);
            dbConnectStatus = DriverManager.getConnection(SERVER, LOGIN, PASSWORD);
            Log.d("SQL", "connect ok");
            return dbConnectStatus;
        } catch (SQLException e) {
            //если нет интернет соиденения
            stopTaskConnectStatus(R.string.toast_prodlenie10);
            Log.d("SQL", e.getMessage());
        }
        return dbConnectStatus;
    }

    //метод select выборки данных из базы user по переданному запросу
    private void selectConnectStatus(String idUser, String md5) {

        String selectSQL = "SELECT\n" +
                "  user.iduser,\n" +
                "  user.md5\n" +
                "FROM user\n" +
                "WHERE user.iduser = \'" + idUser + "\'\n" +
                "AND user.md5 = \'" + md5 + "\'";
        Connection dbConnection = null;
        Statement statement = null;
        try {
            dbConnection = connectStatus();
            statement = dbConnection.createStatement();
            // выбираем данные с БД
            ResultSet resultSet = statement.executeQuery(selectSQL);

            Log.d("SQL", "sql query ok");
            // И если что то было получено то цикл while сработает
            if (resultSet.isBeforeFirst()) {
                while (resultSet.next()) {
                    isValidConnect = true;
                    Log.d("SQL", resultSet.getString("user.iduser")); //выдаю пока в лог
                    Log.d("SQL", resultSet.getString("user.md5")); //выдаю пока в лог
                }
            } else {
                //если результат запроса нулевой
                Log.d("SQL", "sql query null");
                stopTaskConnectStatus(R.string.toast_prodlenie9); //перрываю поток

            }
        } catch (SQLException e) {
            //если ошибка запроса в целом
            stopTaskConnectStatus(R.string.toast_prodlenie8); //перрываю поток
            Log.d("SQL", e.getMessage());
            //закрываю соединения и сессию
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                    Log.d("SQL", "session close");
                } catch (SQLException e) {
                    //если ошибка при закрытии сессии
                    stopTaskConnectStatus(R.string.toast_prodlenie8); //перрываю поток
                    e.printStackTrace();
                    Log.d("SQL", e.getMessage());
                }
            }
            if (dbConnection != null) {
                try {
                    dbConnection.close();
                    Log.d("SQL", "connect close");
                } catch (SQLException e) {
                    //если ошибка при закрытии соиденения
                    stopTaskConnectStatus(R.string.toast_prodlenie8); //перрываю поток
                    e.printStackTrace();
                    Log.d("SQL", e.getMessage());
                }
            }
        }
    } //конец метод select выборки данных из базы user по переданному запросу


    //запускаем поток проверки соединения
    private void startTaskConnectStatus() {
        taskConnectStatus = new TaskConnectStatus();
        taskConnectStatus.execute();

    }

    //прерывание потока с ошибкой
    private void stopTaskConnectStatus(int errMsg) {
        setErrMsgToast(errMsg);
        taskConnectStatus.cancel(true);
    }

    //поток проверки соеденения
    private class TaskConnectStatus extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            Log.d("SQL", "stream run!!!"); //выдаю пока в лог
            selectConnectStatus(fIdUser, fMd5);
            return null;
        }

        protected void onCancelled() {
            super.onCancelled();
            isValidConnect = false;
            showErrToast(ERR_MSG_TOAST, R.color.err_toast);
            Log.d("SQL", "stream abort!!!"); //выдаю пока в лог

        }
    }

    //endregion


    //region методы обработки продления
    //метод insert вставки данных в базу транзакций
    private void insertTableProlongation(String idUser, String prlnGtnDate, String prlnGtn, String[] pool) {
        isIsValidSavePrl = false;
        //запрос для записи транзакции
        String insertSQL = "INSERT INTO prolongation (iduser, prlngtndate, prlngtn)\n" +
                "  VALUES (\'" + idUser + "\', \'" + prlnGtnDate + "\',\'" + prlnGtn + "\')";

        Connection dbConnection = null;
        Statement statement = null;
        try {
            dbConnection = getDBConnection();
            statement = dbConnection.createStatement();
            // добавляем данные в БД с запросом успеха и инкремента
            int resultSet = statement.executeUpdate(insertSQL, Statement.RETURN_GENERATED_KEYS);
            Log.d("SQL", "sql query ok");
            Log.d("SQL", "resultSet:" + resultSet);
            if (resultSet == 1) {
                isIsValidSavePrl = true;
                //успешно записалось
                //выбираем идентификатор транзакции
                fAutoIncKeyFromApi = -1;
                ResultSet resultSetTransaction = statement.getGeneratedKeys();
                if (resultSetTransaction.next()) {
                    //идентификатор транзакции
                    fAutoIncKeyFromApi = resultSetTransaction.getInt(1);
                    Log.d("SQL", "resultSetTransaction:" + fAutoIncKeyFromApi);
                    //запись пула продливаемых книг
                    for (int i = 0; i < pool.length; i++) {
                        isIsIsValidSavePrlBook = false;
                        //запрос для записи пула книг
                        String insertSQLPool = "INSERT INTO prolongationbook (idtrn, barcode)\n" +
                                "  VALUES ('" + fAutoIncKeyFromApi + "', '" + pool[i] + "')";
                        int resultSetPool = statement.executeUpdate(insertSQLPool);
                        Log.d("SQL", "sql query pool ok");
                        Log.d("SQL", "resultSetPool:" + resultSetPool);

                        if (resultSetPool == 1) {
                            isIsIsValidSavePrlBook = true;
                            Log.d("SQL", "send book[" + i + "] ok");

                        } else {
                            //ошибка если какаято книга не добавилась в базу
                            isIsIsValidSavePrlBook = false;
                            Log.d("SQL", "send book[" + i + "] err");
                            //stop stream
                            stopTaskTableProlongationInsert(R.string.toast_prodlenie9);
                        }
                    }

                } else {

                    // throw an exception from here ошибка если не удалось получить номер транзакцию
                    isIsValidSavePrl = false;
                    Log.d("SQL", "resultSetTransaction err!!");
                    //stop stream
                    stopTaskTableProlongationInsert(R.string.toast_prodlenie9);
                }

            } else {
                //ошибка если скрипт не добавил ничего
                isIsValidSavePrl = false;
                Log.d("SQL", "resultSet err!!");
                //stop stream
                stopTaskTableProlongationInsert(R.string.toast_prodlenie9);
            }

            Log.d("SQL", idUser); //выдаю пока в лог
            Log.d("SQL", prlnGtnDate); //выдаю пока в лог
            Log.d("SQL", prlnGtn); //выдаю пока в лог


        } catch (SQLException e) {
            isIsValidSavePrl = false;
            Log.d("SQL", e.getMessage());
            stopTaskTableProlongationInsert(R.string.toast_prodlenie8);
            //закрываю соединения и сессию
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                    Log.d("SQL", "session close");
                } catch (SQLException e) {
                    e.printStackTrace();
                    Log.d("SQL", e.getMessage());
                    stopTaskTableProlongationInsert(R.string.toast_prodlenie8);
                }
            }
            if (dbConnection != null) {
                try {
                    dbConnection.close();
                    Log.d("SQL", "connect close");
                } catch (SQLException e) {
                    e.printStackTrace();
                    Log.d("SQL", e.getMessage());
                    stopTaskTableProlongationInsert(R.string.toast_prodlenie8);
                }
            }
        }
    } //конец метод insert вставки данных в базу транзакций


    private void setResultTableProlongationInsert() {
        //подготовка пула книг
        setPool();
        //подготавливаю дату продления
        fprlnGtnDate = "";
        fprlnGtnDate = DateBackEnterText.getText().toString();
        //по умолчанию транзакция не отработана
        fprlnGtn = "";
        fprlnGtn = "0";
    }

    private void getResultTableProlongationInsert() {
        if (isIsValidSavePrl & isIsIsValidSavePrlBook) {
            clearItem();
            hideRecycler(mAdapter.getItemCount());
            setItemCount(mAdapter.getItemCount());
            Log.d("SQL", "update prolongation ok!!!");
            showErrToast(R.string.toast_prodlenie5, R.color.ap_blue_2);

        } else {
            Log.d("SQL", "update prolongation abort!!!");

        }
        stopLoadProdDialog();

        Log.d("SQL", fIdUser); //выдаю пока в лог
        Log.d("SQL", fprlnGtnDate); //выдаю пока в лог
        Log.d("SQL", fprlnGtn); //выдаю пока в лог


    }

    //метод запуска потока, добавления транзакции
    private void runTaskTableProlongationInsert() {
        //старт потока
        taskTableProlongationInsert = new TaskTableProlongationInsert();
        taskTableProlongationInsert.execute();
    }


    //метод остановки потока с выводом Toast ошибки
    private void stopTaskTableProlongationInsert(int errMsg) {
        setErrMsgToast(errMsg);
        taskTableProlongationInsert.cancel(true);


    }


    //поток добавления транзакции
    private class TaskTableProlongationInsert extends AsyncTask<Void, Void, Void> {

        //получаем значения в потоке из UI
        //выполняется перед doInBackground()
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d("SQL", "stream preparation!!!"); //выдаю пока в лог
            setResultTableProlongationInsert();
        }

        //устанавливаем фоновый режим поток выполнения отправки данных в базу
        @Override
        protected Void doInBackground(Void... voids) {
            Log.d("SQL", "stream run!!!"); //выдаю пока в лог
            insertTableProlongation(fIdUser, fprlnGtnDate, fprlnGtn, bookPool);
            return null;
        }

        //выполняется после doInBackground()
        //передаем значения полученные в потоке на UI
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.d("SQL", "stream stop!!!"); //выдаю пока в лог
            getResultTableProlongationInsert();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            stopLoadProdDialog();
            showErrToast(ERR_MSG_TOAST, R.color.err_toast);
            Log.d("SQL", "stream abort!!!"); //выдаю пока в лог
        }
    }//конец класса потока
    //endregion


    //endregion


//конец класса
}
