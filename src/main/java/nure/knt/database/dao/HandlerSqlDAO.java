package nure.knt.database.dao;

import nure.knt.database.idao.terms.ITermInformation;
import nure.knt.database.idao.tools.IConcatScripts;
import nure.knt.database.idao.tools.IConnectorGetter;
import nure.knt.tools.WorkWithCountries;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

@Component
public class HandlerSqlDAO {

    private static WorkWithCountries countries;

    public static WorkWithCountries getCountries() {
        return countries;
    }
    @Autowired
    public void setCountries(WorkWithCountries countries) {
        HandlerSqlDAO.countries = countries;
    }

    public static final String SORT_TO_DATE_REGISTRATION = " ORDER BY date_registration DESC;";
    public static final int EMPTY_CAPACITY = 0;
    public static final String REPLACE_SYMBOL = "!?#!@#@_REPLACE_ME_@#@!#?!";

    public static final Consumer<PreparedStatement> DEFAULT_PARAMETER = (PreparedStatement p) -> {};

    public static String containingString(String str){
        return "%".concat(str).concat("%");
    }

    public static boolean arrayHasOnlyOne(int[] arr){
        for(int i: arr){
            if(i != 1){
                return false;
            }
        }
        return true;
    }

    public static @NotNull String setInInsideScript(@NonNull String script, Iterable<?> ids){
        return script.replace(HandlerSqlDAO.REPLACE_SYMBOL,HandlerSqlDAO.symbolsInDependsFromSize(ids));
    }

    public static @NotNull String setInInsideScript(@NonNull String script, int lengthArray){
        return script.replace(HandlerSqlDAO.REPLACE_SYMBOL,HandlerSqlDAO.symbolsInDependsFromSize(lengthArray));
    }

    public static String symbolsInDependsFromSize(Iterable<?> ids){
        StringBuilder symbols = new StringBuilder();
        ids.forEach(i -> symbols.append("?,"));
        if(symbols.isEmpty()){
            return "";
        }
        return symbols.substring(0,symbols.length()-1).toString();
    }

    public static String symbolsInDependsFromSize(int lengthArray){
        StringBuilder symbols = new StringBuilder();
        for (int i = 0; i < lengthArray; i++) {
            symbols.append("?,");
        }
        if(symbols.isEmpty()){
            return "";
        }
        return symbols.substring(0,symbols.length()-1).toString();
    }

    public static <T> void substituteIds(java.sql.PreparedStatement preparedStatement, Iterable<T> ids, Function<T,Long> function) throws SQLException {
        int index = 0;
        for (T iter:ids) {
            preparedStatement.setLong(++index,function.apply(iter));
        }
    }

    public static <T> boolean isEmpty(Iterable<T> items){
        return items == null || !items.iterator().hasNext();
    }

    public static String concatScriptToEnd(@NonNull String startScript,@NonNull String ...ExtraScripts){
        if(ExtraScripts.length == 0) {
            return startScript;
        }
        return startScript.replace(";",String.join(" ",ExtraScripts));
    }

    public static String concatScript(@NonNull String startScript,@NonNull String ...ExtraScripts){
        if(ExtraScripts.length == 0) {
            return startScript;
        }
        return String.join(" ",ExtraScripts);
    }

    private static final int START_POSITION = 0;

    public static <T> List<T> useSelectScript(IConnectorGetter connectorGetter, final String script,
                                              Function<ResultSet,T> getObject, @NonNull Object ...array){
        try(java.sql.PreparedStatement stat = connectorGetter.getSqlPreparedStatement(script)) {
            int position = START_POSITION;
            for(Object obj : array){
                position= checkType(position,obj,stat);
            }

            try(ResultSet resultSet = stat.executeQuery()){
                List<T> list = new ArrayList<>();
                while (resultSet.next()){
                    list.add(getObject.apply(resultSet));
                }
                return list;
            }

        }catch (SQLException e){
            e.printStackTrace();
            return new ArrayList<>(HandlerSqlDAO.EMPTY_CAPACITY);
        }
    }

    public static int checkType(int position,Object obj,java.sql.PreparedStatement statement) throws SQLException {
        if(obj instanceof Iterable){
            Iterable<?> iter = (Iterable<?>) obj;
            for (Object it:iter) {
                position = checkType(position,it,statement);
            }
        }else if(obj instanceof Object[]){
            Object[] array = (Object[]) obj;
            for (Object i:array) {
                position = checkType(position,i,statement);
            }
        }
        else {
            substituteVariable(statement,++position,obj);
        }

        return position;
    }

    private static void substituteVariable(PreparedStatement preparedStatement,int position,Object object) throws SQLException{
        if(object == null){
            preparedStatement.setNull(position, Types.OTHER);
            return;
        }

        if (object instanceof Long) {
            preparedStatement.setLong(position, (Long) object);
        } else if (object instanceof String) {
            preparedStatement.setString(position, object.toString());
        } else if (object instanceof Integer){
            preparedStatement.setInt(position, (Integer) object);
        } else if (object instanceof LocalDateTime){
            preparedStatement.setTimestamp(position, Timestamp.valueOf(((LocalDateTime) object)));
        } else if( object instanceof LocalDate){
            preparedStatement.setDate(position, Date.valueOf((LocalDate) object));
        } else if (object instanceof Boolean) {
            preparedStatement.setBoolean(position, (Boolean) object);
        } else if (object instanceof Float){
            preparedStatement.setFloat(position, (Float) object);
        } else if (object instanceof Enum<?>){
            preparedStatement.setString(position, object.toString());
        }else{
            throw new SQLException("substituteVariable: object is " + object.getClass().getName());
        }
    }

    public static void setFieldsInsideScript(PreparedStatement statement,Iterable<?> objects) throws SQLException {
        int position = START_POSITION;
        for (Object object:objects) {
            position = checkType(position,object,statement);
        }
    }

    @Nullable
    public static <T> T useSelectScriptAndGetOneObject(IConnectorGetter connectorGetter, final String script, Function<ResultSet, T> getObject, @NonNull Object ...array){
        try(java.sql.PreparedStatement stat = connectorGetter.getSqlPreparedStatement(script)) {
            int position = START_POSITION;
            for(Object obj : array){
                position= checkType(position,obj,stat);
            }
            try(ResultSet resultSet = stat.executeQuery()){
                if(resultSet.next()){
                    return getObject.apply(resultSet);
                }
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        return null;
    }

    // new 03.06.2022
    public static final int[] ERROR_UPDATE = new int[]{Integer.MIN_VALUE,Integer.MAX_VALUE};
    public static final boolean ERROR_BOOLEAN_ANSWER = false;
    public static final boolean HAVE_NO_ERROR = true;

    public static <T> int[] updateById(IConnectorGetter connectorGetter, final String script, Iterable<T> collection, BiFunction<PreparedStatement,T,Boolean> setField){

        try(PreparedStatement statement = connectorGetter.getSqlPreparedStatement(script)){

            for (T entity: collection) {
                if(setField.apply(statement,entity) == ERROR_BOOLEAN_ANSWER){
                    return ERROR_UPDATE;
                }
                statement.addBatch();
            }
            return statement.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ERROR_UPDATE;
    }

    // new 05.06.2022
    public static final int ERROR_DELETE = Integer.MIN_VALUE;

    public static <T>  int deleteByIdIn(IConnectorGetter connectorGetter, String script,Iterable<T> collection,Function<T,Long> entityToLong){

        if(script.contains(HandlerSqlDAO.REPLACE_SYMBOL)){
            script = setInInsideScript(script,collection);
        }

        try(PreparedStatement statement = connectorGetter.getSqlPreparedStatement(script)){

            HandlerSqlDAO.substituteIds(statement,collection,entityToLong);

            return statement.executeUpdate();


        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ERROR_DELETE;
    }

    private static final Function<Long,Long> LONG_TO_LONG = (id) -> id;

    public static int deleteByIdIn(IConnectorGetter connectorGetter, String script,Iterable<Long> collection){
        return HandlerSqlDAO.deleteByIdIn(connectorGetter,script,collection,LONG_TO_LONG);
    }

    public static <E> Map<E,String> setNameScriptForEntityByValueUnmodifiable(String fileName, String propertyStart, E[] enums){
        return Collections.unmodifiableMap(HandlerSqlDAO.setNameScriptForEntityByValue(fileName,propertyStart,enums));
    }

    public static <E> Map<E,String> setNameScriptForEntityByValue(String fileName, String propertyStart, E[] enums){
        HashMap<E,String> map = new HashMap<>();

        Properties appProps = new Properties();

        try(FileInputStream fileInputStream = new FileInputStream(fileName)) {
            appProps.load(fileInputStream);

            map.put(null,appProps.getProperty(propertyStart + "error"));

            for (E enumObject: enums) {
                map.put(enumObject,appProps.getProperty(propertyStart + enumObject,"Error."+enumObject));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return map;
    }


    public static String toScriptDefault(String SELECT_AND_FIELD,String FROM_AND_JOIN,ITermInformation iterm, IConcatScripts concator){
        return concator.concatScripts("",
                SELECT_AND_FIELD,
                iterm.getSelectField(),
                FROM_AND_JOIN,
                iterm.getJoin(),
                (iterm.getWhere().isEmpty()) ? "" : " WHERE " + iterm.getWhere(),
                (iterm.getGroupBy().isEmpty()) ? "" : " GROUP BY " + iterm.getGroupBy(),
                (iterm.getHaving().isEmpty()) ? "" : " HAVING " + iterm.getHaving(),
                iterm.getOrderBy(),
                iterm.getLimit());
    }

    public static <Entity,Field> int[] updateByFieldAndId(IConnectorGetter connector,
                                                          String script,Iterable<? extends Entity> entities,
                                                          Map<? extends Field,Function<Entity,Object>> map,
                                                          Field[] fields,Field id){
        try(PreparedStatement statement = connector.getSqlPreparedStatement(script)) {
            List<Object> listFields = new ArrayList<>(getCapacity(fields));

            for (Entity entity:entities) {
                for (int i = 0; i < fields.length; i++) {
                    listFields.add(map.get(fields[i]).apply(entity));
                }
                listFields.add(map.get(id).apply(entity));

                setFieldsInsideScript(statement,listFields);
                statement.addBatch();
                listFields.clear();
            }
            return statement.executeBatch();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ERROR_UPDATE;
    }

    private static int getCapacity(Object[] array){
        return array.length +1;
    }

    public static <Field> String getScript(Map<Field,String> mapFields,Field[] fields,String addedPiece,String joinPiece){

        String script = "";

        for(Field field: fields){
            script = toJoin(script,mapFields.get(field).concat(addedPiece),joinPiece);
        }

        return script;
    }

    public static String toJoin(String whereJoin,String whatJoin,String joinPiece){
        if(whereJoin.isEmpty()){
            return whatJoin;
        }
        return String.join(joinPiece,whereJoin,whatJoin);
    }
}
