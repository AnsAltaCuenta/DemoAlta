package com.accenture.demoalta;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by salvador.ruiz on 02/06/2017
 *            karen.torres  on 02/06/2017.
 */

public class Formatter {
    public boolean contieneFecha(String cadenaIFE){
        String regexp = "\\d{1,2}/\\d{1,2}/\\d{4}";

        Pattern pattern1 = Pattern.compile(regexp);
        Matcher matcher1 = pattern1.matcher(cadenaIFE);
        return matcher1.find();
    }

    public String quitarFecha(String cadenaIFE){
        int inicio = 0, fin = 0;
        StringBuilder cadenaIFEsinFecha = new StringBuilder();
        String cadena = "";
        String regexp = "\\d{1,2}/\\d{1,2}/\\d{4}";

        Pattern pattern1 = Pattern.compile(regexp);
        Matcher matcher1 = pattern1.matcher(cadenaIFE);

        for (int i = 0; i < 1; i++) {
            while (matcher1.find()) {
                //System.out.println(matcher1.group(0));
                inicio = matcher1.start();
                fin = matcher1.end();
            }
        }

        cadenaIFEsinFecha.append(cadenaIFE);
        cadenaIFEsinFecha.delete(inicio, fin + 1);

        cadena = cadenaIFEsinFecha.toString();
        cadena = cadena.replaceAll("FECHANACIMIENTO", "");
        cadena = cadena.replaceAll("FECHA DE NACIMIENTO", "");

        return cadena;
    }

    public String quitarEtiquetasIFE(String cadenaIFE){
        int inicio = 0, fin = 0;
        StringBuilder sb;

        Pattern p = Pattern.compile("(?:SEXO)|(?:SEX)");
        Matcher m = p.matcher(cadenaIFE);

        if(m.find()){
            inicio = m.start();
            fin = m.end();

            sb = new StringBuilder();
            sb.append(cadenaIFE);
            sb.delete(inicio, fin );
            //sb.delete(inicio, fin + 3);
            cadenaIFE = sb.toString();
        }

        p = Pattern.compile("EDAD");
        m = p.matcher(cadenaIFE);

        if(m.find()){
            inicio = m.start();
            fin = m.end();

            sb = new StringBuilder();
            sb.append(cadenaIFE);
            sb.delete(inicio, fin + 4 );
            cadenaIFE = sb.toString();
        }

        p = Pattern.compile("NOMBRE");
        m = p.matcher(cadenaIFE);

        if(m.find()){
            inicio = m.start();
            fin = m.end();

            sb = new StringBuilder();
            sb.append(cadenaIFE);
            sb.delete(inicio, fin + 1 );
            cadenaIFE = sb.toString();
        }

        p = Pattern.compile("DOMICILIO");
        m = p.matcher(cadenaIFE);

        if (m.find()){
            inicio = m.start();
            fin = m.end();

            sb = new StringBuilder();
            sb.append(cadenaIFE);
            sb.delete(inicio,fin +1);
            cadenaIFE = sb.toString();
        }

        p = Pattern.compile("CURP");
        m = p.matcher(cadenaIFE);

        if (m.find()){
            inicio = m.start();
            fin = m.end();

            sb = new StringBuilder();
            sb.append(cadenaIFE);
            sb.delete(inicio,fin+1);
            cadenaIFE = sb.toString();
        }

        return cadenaIFE;
    }

    public String obtenerCadenaNombre(String cadEscaneada){
        int inicioNombre = 0,finNombre = 0, inicioDomicilio = 0, finDomicilio = 0;
        StringBuilder sb = new StringBuilder();

        Pattern p = Pattern.compile("NOMBRE");
        Matcher m = p.matcher(cadEscaneada);

        if(m.find()){
            inicioNombre = m.start();
        }

        p = Pattern.compile("DOMICILIO");
        m = p.matcher(cadEscaneada);

        if(m.find()){
            inicioDomicilio = m.start();
        }

        cadEscaneada = cadEscaneada.substring(inicioNombre, inicioDomicilio);
        System.out.println("cadEscaneada: " + cadEscaneada);

        return cadEscaneada;
    }



}
