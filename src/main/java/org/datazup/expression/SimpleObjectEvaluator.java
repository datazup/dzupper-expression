package org.datazup.expression;


import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.datazup.expression.exceptions.NotSupportedExpressionException;
import org.datazup.pathextractor.AbstractVariableSet;
import org.datazup.utils.DateTimeUtils;
import org.joda.time.DateTime;

import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ninel on 3/14/16.
 */
public class SimpleObjectEvaluator extends AbstractEvaluator<Object> {
    /**
     * The negate unary operator.
     */
    public final static Operator NEGATE = new Operator("!", 1, Operator.Associativity.RIGHT, 3);
    /**
     * The logical AND operator.
     */
    private static final Operator AND = new Operator("&&", 2, Operator.Associativity.LEFT, 2);
    /**
     * The logical OR operator.
     */
    public final static Operator OR = new Operator("||", 2, Operator.Associativity.LEFT, 1);

    public final static Operator NOT_EQUAL = new Operator("!=", 2, Operator.Associativity.LEFT, 4);
    public final static Operator EQUAL = new Operator("==", 2, Operator.Associativity.LEFT, 5);
    public final static Operator GREATER_THEN = new Operator(">", 2, Operator.Associativity.LEFT, 6);
    public final static Operator LOWER_THEN = new Operator("<", 2, Operator.Associativity.LEFT, 7);

    public final static Operator PLUS = new Operator("+", 2, Operator.Associativity.LEFT, 8);
    public final static Operator MINUS = new Operator("-", 2, Operator.Associativity.LEFT, 9);


    public final static Function IS_NULL = new Function("IS_NULL", 1);
    public final static Function SET_NULL = new Function("SET_NULL", 1);
    public final static Function SIZE_OF = new Function("SIZE_OF", 1);


    // date functions
    public final static Function NOW = new Function("NOW", 0);
    public final static Function STR_TO_DATE_TIMESTAMP = new Function("STR_TO_DATE_TIMESTAMP", 2);

    public final static Function MINUTE = new Function("MINUTE", 1);
    public final static Function HOUR = new Function("HOUR", 1);
    public final static Function DAY = new Function("DAY", 1);
    public final static Function WEEK = new Function("WEEK", 1);
    public final static Function MONTH = new Function("MONTH", 1);
    public final static Function YEAR = new Function("YEAR", 1);

    public final static Function TO_DATE = new Function("TO_DATE", 2);
    public final static Function TO_INT = new Function("TO_INT", 1);
    public final static Function TO_LONG = new Function("TO_LONG", 1);
    public final static Function TO_DOUBLE = new Function("TO_DOUBLE", 1);
    public final static Function TO_STRING = new Function("TO_STRING", 1);

    public final static Function REGEX_MATCH= new Function("REGEX_MATCH", 2);
    public final static Function REGEX_EXTRACT= new Function("REGEX_EXTRACT",2,3);
    public final static Function EXTRACT= new Function("EXTRACT",2);

    public final static Function STRING_FORMAT = new Function("STRING_FORMAT", 2, Integer.MAX_VALUE);


    // public final static Function DATE = new Function("DATE", 2);


    protected static final Parameters PARAMETERS;

    static {
        // Create the evaluator's parameters
        PARAMETERS = new Parameters();
        // Add the supported operators
        PARAMETERS.add(AND);
        PARAMETERS.add(OR);
        PARAMETERS.add(NEGATE);

        PARAMETERS.add(NOT_EQUAL);
        PARAMETERS.add(EQUAL);
        PARAMETERS.add(GREATER_THEN);
        PARAMETERS.add(LOWER_THEN);

        PARAMETERS.add(PLUS);
        PARAMETERS.add(MINUS);

        PARAMETERS.addExpressionBracket(BracketPair.PARENTHESES);
        PARAMETERS.addFunctionBracket(BracketPair.PARENTHESES);
        PARAMETERS.add(NOW);
        PARAMETERS.add(IS_NULL);
        PARAMETERS.add(SET_NULL);
        PARAMETERS.add(SIZE_OF);

        PARAMETERS.add(STR_TO_DATE_TIMESTAMP);

        PARAMETERS.add(MINUTE);
        PARAMETERS.add(HOUR);
        PARAMETERS.add(DAY);
        PARAMETERS.add(WEEK);
        PARAMETERS.add(MONTH);
        PARAMETERS.add(YEAR);

        PARAMETERS.add(TO_DATE);
        PARAMETERS.add(TO_INT);
        PARAMETERS.add(TO_LONG);
        PARAMETERS.add(TO_DOUBLE);
        PARAMETERS.add(TO_STRING);

        PARAMETERS.add(STRING_FORMAT);

        PARAMETERS.add(REGEX_MATCH);
        PARAMETERS.add(REGEX_EXTRACT);
        PARAMETERS.add(EXTRACT);
   }

    private static SimpleObjectEvaluator INSTANCE;

    public static SimpleObjectEvaluator getInstance(){
        SimpleMapListResolver simpleMapListResolver = new SimpleMapListResolver();
        return getInstance(simpleMapListResolver);
    }


    public static SimpleObjectEvaluator getInstance(AbstractMapListResolver mapListResolver){
        synchronized (SimpleObjectEvaluator.class){
            if (null==INSTANCE){
                synchronized (SimpleObjectEvaluator.class){
                    if (null==INSTANCE)
                        INSTANCE = new SimpleObjectEvaluator(mapListResolver);
                }
            }
        }
        return INSTANCE;
    }

    public SimpleObjectEvaluator() {
        super(PARAMETERS, new SimpleMapListResolver());
    }
    public SimpleObjectEvaluator(AbstractMapListResolver mapListResolver) {
        super(PARAMETERS, mapListResolver);
    }

    @Override
    protected Object toValue(String literal, Object evaluationContext) {
        if (evaluationContext instanceof AbstractVariableSet) {
            AbstractVariableSet abs = (AbstractVariableSet) evaluationContext;
            Object o = abs.get(literal);
            if (null == o) {
                return new NullObject();
            } else {
                return o;
            }
        }
        return literal;
    }

    @Override
    protected Object evaluate(Function function, Iterator<Object> operands, Deque<Token> argumentList, Object evaluationContext) {

        if (function==STRING_FORMAT){
            return stringFormat(function, operands, argumentList, evaluationContext);
        }else if (function == TO_DATE){
            return toDate(function, operands, argumentList, evaluationContext);
        }
        else if (function == TO_INT){
            return toInteger(function, operands, argumentList, evaluationContext);
        }
        else if (function == TO_DOUBLE){
            return toDouble(function, operands, argumentList, evaluationContext);
        }
        else if (function == TO_LONG){
            return toLong(function, operands, argumentList, evaluationContext);
        }
        else if (function == TO_STRING){
            return toStringValue(function, operands, argumentList, evaluationContext);
        }
        else if (function == STR_TO_DATE_TIMESTAMP) {
            return strToDateTimeStamp(function, operands, argumentList, evaluationContext);
        } else if (function == MINUTE) {
            Object op1 = operands.next();
            argumentList.pop();
            Instant dt = DateTimeUtils.resolve(op1);
            return DateTimeUtils.getMinute(dt); //LocalDateTime.ofInstant(dt, ZoneOffset.UTC).getMinute();
        } else if (function == HOUR) {
            Object op1 = operands.next();
            argumentList.pop();
            Instant dt = DateTimeUtils.resolve(op1);
            return DateTimeUtils.getHour(dt);
        } else if (function == DAY) {
            Object op1 = operands.next();
            argumentList.pop();
            Instant dt = DateTimeUtils.resolve(op1);
            return DateTimeUtils.getDayOfMonth(dt);
        } else if (function == WEEK) {
            Object op1 = operands.next();
            argumentList.pop();
            Instant dt = DateTimeUtils.resolve(op1);
            int res = DateTimeUtils.getDayOfMonth(dt) % 7;
            return res;
        } else if (function == MONTH) {
            Object op1 = operands.next();
            argumentList.pop();
            Instant dt = DateTimeUtils.resolve(op1);
            return DateTimeUtils.getMonth(dt);
        } else if (function == YEAR) {
            Object op1 = operands.next();
            argumentList.pop();
            Instant dt = DateTimeUtils.resolve(op1);
            return DateTimeUtils.getYear(dt);
        } else if (function == NOW) {
            return System.currentTimeMillis();
        } else if (function == SET_NULL) {
            Object op1 = operands.next();
            argumentList.pop();
            return null; //getNullObject();
        } else if (function == IS_NULL) {
            Object op1 = operands.next();
            Token token = argumentList.pop();
            if (null == op1 || op1 instanceof NullObject || op1.toString() == token.getContent().toString()) {
                return true;
            }
            return op1 == null;
        } else if (function == SIZE_OF) {
            Object op1 = operands.next();
            argumentList.pop();
            if (null != op1 && !(op1 instanceof NullObject)) {

                Map m = mapListResolver.resolveToMap(op1);
                if (null==m){
                    Collection c = mapListResolver.resolveToCollection(op1);
                    if (null==c){
                        List l = mapListResolver.resolveToList(op1);
                        if (null!=l){
                            return l.size();
                        }
                    }else{
                        return c.size();
                    }
                }else{
                    return m.size();
                }

                throw new NotSupportedExpressionException("SizeOf function not supported for instance of \"" + op1.getClass() + "\"");

                /*
                if (op1 instanceof Collection) {
                    Collection c = (Collection) op1;
                    return c.size();
                } else if (op1 instanceof Map) {
                    Map map = (Map) op1;
                    return map.size();
                } else if (op1 instanceof String) {
                    return ((String) op1).length();
                } else {
                    throw new NotSupportedExpressionException("SizeOf function not supported for instance of \"" + op1.getClass() + "\"");
                }*/
            }
            return 0;
        } else if (function == REGEX_MATCH) {

            return regexMatch(function, operands, argumentList, evaluationContext);
        } else if (function == REGEX_EXTRACT) {

            return regexExtract(function, operands, argumentList, evaluationContext);
        }
    	else if (function==EXTRACT){
            
            return extract(function, operands, argumentList, evaluationContext);
        }
        else {
            return nextFunctionEvaluate(function, operands, argumentList, evaluationContext);
        }
    }

    private Object stringFormat(Function function, Iterator<Object> operands, Deque<Token> argumentList, Object evaluationContext) {
        Object valueObject = operands.next();
        argumentList.pop();

        List<Object> payload = new ArrayList<>();
        while (operands.hasNext()){
            Object val = operands.next();
            argumentList.pop();
            payload.add(val);
        }

        if (null!=valueObject){
            if (valueObject instanceof String){
                Object[] arr = payload.toArray(new Object[payload.size()]);
                String valueResult = String.format((String)valueObject, arr);
                if (!StringUtils.isEmpty(valueResult) && valueResult.startsWith("#") && valueResult.endsWith("#")){
                    valueResult = valueResult.substring(1, valueResult.length()-1);
                }
                return  valueResult;
            }
        }


        return null;
    }

    private Object toStringValue(Function function, Iterator<Object> operands, Deque<Token> argumentList, Object evaluationContext) {
        Object valueObject = operands.next();
        argumentList.pop();

        if (null==valueObject)
            return valueObject;

        return valueObject.toString();
    }

    private Object toLong(Function function, Iterator<Object> operands, Deque<Token> argumentList, Object evaluationContext) {
        Object valueObject = operands.next();
        argumentList.pop();

        if (null==valueObject)
            return valueObject;

        Number number = resolveNumber(valueObject);
        if (null==number){
            return number;
        }
        return resolveNumber(valueObject).longValue();
    }

    private Object toDouble(Function function, Iterator<Object> operands, Deque<Token> argumentList, Object evaluationContext) {
        Object valueObject = operands.next();
        argumentList.pop();

        if (null==valueObject)
            return valueObject;

        Number number = resolveNumber(valueObject);
        if (null==number){
            return number;
        }
        return resolveNumber(valueObject).doubleValue();
    }

    private Object toInteger(Function function, Iterator<Object> operands, Deque<Token> argumentList, Object evaluationContext) {

        Object valueObject = operands.next();
        argumentList.pop();

        if (null==valueObject)
            return valueObject;

        Number number = resolveNumber(valueObject);
        if (null==number){
            return number;
        }
        return resolveNumber(valueObject).intValue();
    }

    private Number resolveNumber(Object valueObject){
        Number num = null;

        if(valueObject instanceof Number){
            num = (Number)valueObject;
        }else if (valueObject instanceof String){
            num = NumberUtils.createNumber((String)valueObject);
        }

        return num;
    }


    private Object extract(Function function, Iterator<Object> operands, Deque<Token> argumentList,
			Object evaluationContext) {

    	String fieldValue = operands.next().toString();
    	//String[] fieldValueSplited = fieldValue.toLowerCase().split("\\b");
        Token token = argumentList.pop();

        String topicValues = operands.next().toString();
        topicValues = topicValues.replaceAll("#", "");
        Token token1 = argumentList.pop();
        
        String[] searchedTopics =  topicValues.toLowerCase().split(",");
        
        ArrayList list = new ArrayList();
        for(int i =0; i<searchedTopics.length; i++){
            String k = searchedTopics[i].trim();
            boolean exists = false;
            if (k.contains(" ")){
                String[] sk = k.split(" ");
                for (int j =0;j<sk.length;j++){
                    String sk1 = sk[j];
                    if (fieldValue.contains(sk1)){
                        exists = true;
                        break;
                    }
                }
            }else{
                if(fieldValue.toLowerCase().contains(k)){
                    exists = true;
                }
            }
    	    if(exists){
    	        list.add(k);
    	    }
        }
        
		return list;
	}

	private Object regexExtract(Function function, Iterator<Object> operands, Deque<Token> argumentList,
			Object evaluationContext) {
		
    	String regexFieldValue = operands.next().toString();
        Token token = argumentList.pop();

        String regexPattern = operands.next().toString();
        Token token1 = argumentList.pop();

        Object group = null;
        if (operands.hasNext()) {
            group = operands.next();
            Token token2 = argumentList.pop();
        }

        regexPattern = regexPattern.replace("#", "");
        Pattern r = Pattern.compile(regexPattern);
        Matcher matcher = r.matcher(regexFieldValue);

        int pos;
        ArrayList<String> list = new ArrayList<String>();
        	
        for(pos = 0; matcher.find(); pos = matcher.end()) {
        	
        	if( null != group){
        		list.add(matcher.group(((Double)group).intValue()));
        	}
        	else{
        		list.add(regexFieldValue.substring(matcher.start(),matcher.end()));
        	}       	
        }
        
        if(list.size() > 1){
        	return list;
        }
        
        return list.size() !=0 ? list.get(0).toString():"";
	}

	private Object regexMatch(Function function, Iterator<Object> operands, Deque<Token> argumentList,
			Object evaluationContext) {
    	 String regexFieldValue = operands.next().toString();
         Token token = argumentList.pop();

         String regexPattern = operands.next().toString();
         Token token1 = argumentList.pop();
         
         Pattern r = Pattern.compile(regexPattern);
         Matcher matcher = r.matcher(regexFieldValue);
         
         boolean matches = matcher.matches();
         
         return matches;
	}

    private Object toDate(Function function, Iterator<Object> operands, Deque<Token> argumentList, Object evaluationContext) {

        Object valueObject = operands.next();
        Token valueObjectToken = argumentList.pop();
        String formatObject = (String) operands.next();
        Token formatObjectToken = argumentList.pop();

        String format = null;
        if (null!=formatObject){
            format = formatObject.replace("#", "");
        }


        Instant leftDateTime = null;

        if (valueObject instanceof String){
            String strDateTime =(String)valueObject;

            // first try to resolve as object
            Instant dt = DateTimeUtils.resolve(valueObject);
            if (null==dt){
                dt = DateTimeUtils.resolve(strDateTime, format);
            }
            if (null!=dt){

                leftDateTime = DateTimeUtils.format(dt, format);

               /* DateTimeFormatter formatter =  DateTimeFormat.forPattern(format).withLocale(Locale.ENGLISH);
                String s = dt.toString(format);
                leftDateTime = formatter.withZoneUTC().parseDateTime(s);*/
            }

        }else if (valueObject instanceof DateTime){
            Instant dt = DateTimeUtils.format((DateTime)valueObject, format);
            leftDateTime = dt;
            //leftDateTime = ((DateTime)valueObject).toDateTime(DateTimeZone.UTC);
        }else if (valueObject instanceof Date){
            //leftDateTime = new DateTime(((Date)valueObject).getTime(), DateTimeZone.UTC);
            Instant dt = DateTimeUtils.format((Date)valueObject, format);
            leftDateTime = dt;
        }else if (valueObject instanceof Number){
            Number valueNumber = (Number)valueObject;
            Long value = valueNumber.longValue();
            Instant dt = DateTimeUtils.format(value, format);
            leftDateTime = dt;
           // leftDateTime = new DateTime(value, DateTimeZone.UTC);

        }

        if (null!=leftDateTime){
            /*DateTimeFormatter formatter =  DateTimeFormat.forPattern(format).withLocale(Locale.ENGLISH);
            String val = leftDateTime.toString(formatter);
            DateTime dateTime = formatter.withZoneUTC().parseDateTime(val);*/

            Instant i = DateTimeUtils.format(leftDateTime, format);

            return i; //leftDateTime.atOffset(ZoneOffset.UTC);//dateTime;
        }

        return null;
    }

	private Object strToDateTimeStamp(Function function, Iterator<Object> operands, Deque<Token> argumentList, Object evaluationContext) {

        String dateString = operands.next().toString().replace("#", "");

        Token token = argumentList.pop();

        String stringFormat = operands.next().toString().replace("#", "");
        Token token1 = argumentList.pop();

        if (stringFormat instanceof String && dateString instanceof String) {
            try {
                /*DateTimeFormatter df = DateTimeFormat.forPattern(stringFormat);
                DateTime dt = DateTime.parse(dateString, df);*/
                Instant dt = DateTimeUtils.resolve(dateString, stringFormat);
                return dt.toEpochMilli();
            } catch (Exception e) {
                System.out.println("Parse date error for date: " + dateString + " and format: " + stringFormat + " - " + e.getMessage());
            }
        }

        return null;
    }

    protected Object nextFunctionEvaluate(Function function, Iterator<Object> operands, Deque<Token> argumentList, Object evaluationContext) {
        return super.evaluate(function, operands, argumentList, evaluationContext);
    }

    protected Object superFunctionEvaluate(Function function, Iterator<Object> operands, Deque<Token> argumentList, Object evaluationContext) {
        return super.evaluate(function, operands, argumentList, evaluationContext);
    }

    @Override
    protected Object evaluate(Operator operator, Iterator<Object> operands, Object evaluationContext) {
        if (operator == NEGATE) {
            Object next = operands.next();
            if (next == null) {
                return false;
            }
            if (next instanceof Boolean) {
                return !((Boolean) next);
            }
        } else if (operator == NOT_EQUAL) {
            Object left = operands.next();
            Object right = operands.next();
            if (left instanceof Number && right instanceof Number) {
                return ((Number) left).doubleValue() != ((Number) right).doubleValue();
            } else {
                return !left.equals(right);
            }
        } else if (operator == EQUAL) {
            Object left = operands.next();
            Object right = operands.next();
            if (left instanceof Number && right instanceof Number) {
                return ((Number) left).doubleValue() == ((Number) right).doubleValue();
            } else {
                return left.equals(right);
            }
        } else if (operator == GREATER_THEN) {
            Object left = operands.next();
            Object right = operands.next();
            // check this: http://stackoverflow.com/questions/2683202/comparing-the-values-of-two-generic-numbers
            Number l = (Number) left;
            Number r = (Number) right;
            //   if (null!=l && null!=r)
            return l.doubleValue() > r.doubleValue();
            //   else return false;
        } else if (operator == LOWER_THEN) {
            Object left = operands.next();
            Object right = operands.next();
            // check this: http://stackoverflow.com/questions/2683202/comparing-the-values-of-two-generic-numbers
            Number l = (Number) left;
            Number r = (Number) right;
            // if (null!=l && null!=r)
            return l.doubleValue() < r.doubleValue();
            // else return false;
        } else if (operator == AND) {
            Object left = operands.next();
            Object right = operands.next();
            boolean l = (Boolean) left;
            boolean r = (Boolean) right;
            return l && r; //Boolean.logicalAnd(l,r);
        } else if (operator == OR) {
            Object left = operands.next();
            Object right = operands.next();
            boolean l = (Boolean) left;
            boolean r = (Boolean) right;
            return l || r; //Boolean.logicalOr(l,r);
        } else if (operator == PLUS) {
            Object left = operands.next();
            Object right = operands.next();
            if (left instanceof String || right instanceof String) {
                String l = null;
                String r = null;
                if (left instanceof String) {
                    l = (String) left;
                } else {
                    l = left.toString();
                }
                if (right instanceof String) {
                    r = (String) right;
                } else {
                    r = right.toString();
                }
                return l + r;
            } else {
                Number l = (Number) left;
                if (null==l)
                    l=0;
                Number r = (Number) right;
                if (null==r)
                    r = 0;
                return l.doubleValue() + r.doubleValue();
            }

        } else if (operator == MINUS) {
            Object left = operands.next();
            Object right = operands.next();
            Number l = (Number) left;
            Number r = (Number) right;
            // if (null!=l && null!=r)
            return l.doubleValue() - r.doubleValue();
        } else {
            return nextOperatorEvaluate(operator, operands, evaluationContext);
        }
        return false;
    }

    protected Object nextOperatorEvaluate(Operator operator, Iterator<Object> operands, Object evaluationContext) {
        return super.evaluate(operator, operands, evaluationContext);
    }

    protected Object superOperatorEvaluate(Operator operator, Iterator<Object> operands, Object evaluationContext) {
        return super.evaluate(operator, operands, evaluationContext);
    }

    protected String normalizeTokenName(String tokenName) {
        if (tokenName.startsWith("$") && tokenName.endsWith("$")) {
            tokenName = tokenName.substring(1, tokenName.length() - 1);
        }
        if (tokenName.contains(".")) {
            StringBuilder builder = new StringBuilder();
            String[] splitted = tokenName.split("\\.");
            int counter = 0;
            for (String s : splitted) {
                if (counter == 0) {
                    builder.append(s);
                } else {
                    String sc = StringUtils.capitalize(s);
                    builder.append(sc);
                }
                counter++;
            }
            tokenName = builder.toString();
        }
        return tokenName;
    }



}
