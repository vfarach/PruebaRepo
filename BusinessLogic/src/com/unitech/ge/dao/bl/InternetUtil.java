package com.unitech.ge.dao.bl;

import com.unitech.ge.framework.CommonServiceImpl;
import com.unitech.ge.framework.UUtil;
import com.unitech.ge.framework.exceptions.NoDataFoundException;
import com.unitech.ge.framework.exceptions.USystemException;
import com.unitech.ge.framework.util.Convert;
import com.unitech.ge.framework.util.UAssert;
import com.unitech.ge.framework.util.UConsultas;
import com.unitech.ge.framework.util.UEncrypter;
import com.unitech.ge.framework.util.UFechas;
import com.unitech.ge.framework.util.UService;
import com.unitech.ge.messages.UMensajes;

import java.sql.SQLException;


import java.util.List;
import java.util.Map;


import oracle.jbo.domain.Date;
import oracle.jbo.domain.BlobDomain;
import oracle.jbo.domain.Number;
import oracle.jbo.server.DBTransaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class InternetUtil {
    static final Logger LOG = LoggerFactory.getLogger(InternetUtil.class);

    public InternetUtil() {
    }

    /**
     * 
     * @param tr Transacción de trabajo
     * @param circunscripcion Código de la circunscripción del abogado
     * @param colegio Código del colegio del abogado
     * @param matricula Matrícula del abogado
     * @param clave Clave del abogado
     * @return ID del profesional (abogado)
     */
    public static Number getProIdAbogado(DBTransaction tr, Number circunscripcion, Number colegio, String matricula, String clave) throws Exception {
        if (circunscripcion == null) {
            LOG.error(UMensajes.PARAMETRO_NULO,"circunscripcion");
            throw new USystemException("El parámetro circunscripcion es nulo.");
        }
        if (UAssert.empty(colegio)) {
            LOG.error(UMensajes.PARAMETRO_NULO,"colegio");
            throw new USystemException("El parámetro colegio es nulo.");
        }
        if (UAssert.empty(matricula)) {
            LOG.error(UMensajes.PARAMETRO_NULO,"matricula");
            throw new USystemException("El parámetro matricula es nulo.");
        }
        if (UAssert.empty(clave)) {
            LOG.error(UMensajes.PARAMETRO_NULO,"clave");
            throw new USystemException("El parámetro clave es nulo.");
        }
        
        String query = "SELECT Profesional.PRO_ID \n" +
            "FROM Profesional \n" +
            "WHERE Profesional.ESTADO = 0 \n" +
            "AND Profesional.PRO_CIRCUNSCRIPCION = ? \n" + 
            "AND Profesional.PRO_COLEGIO = ? \n" +
            "AND Profesional.PRO_MATRICULA = ? \n" +
            "AND Profesional.PRO_CLAVE = ?";
            
        String claveEncriptada = UEncrypter.encripClaveExp(new Integer(clave));
        List<Object> params = UUtil.getList(circunscripcion,colegio,matricula.toUpperCase(),claveEncriptada);
        try {
            return UConsultas.getValueAsNumber(tr,query,params);
        } catch (NoDataFoundException e) {
            return null;
        }
    }


    /**
     * 
     * @param tr Transacción de trabajo.
     * @param usuario Nombre del usuario.
     * @param clave Clave del abogado.
     * @return ID del profesional (abogado).
     */
    public static Number getProIdMagistrado(DBTransaction tr, String usuario, String clave) throws Exception {
        if (UAssert.empty(usuario)) {
            LOG.error(UMensajes.PARAMETRO_NULO,"usuario");
            throw new USystemException("El parámetro usuario es nulo.");
        }
        if (UAssert.empty(clave)) {
            LOG.error(UMensajes.PARAMETRO_NULO,"clave");
            throw new USystemException("El parámetro clave es nulo.");
        }
        
        String query = "SELECT usuario_iol.usiol_ID \n" +
            "FROM usuario_iol \n" +
            "WHERE usuario_iol.ESTADO = 0 \n" + 
            "AND UPPER(usuario_iol.USIOL_NOMBRE) = ? \n" +
            "AND usuario_iol.USIOL_CLAVE = ?";
            
        String claveEncriptada = UEncrypter.encripClaveExp(new Integer(clave));
        List<Object> params = UUtil.getList(usuario.toUpperCase(),claveEncriptada);
        try {
            return UConsultas.getValueAsNumber(tr,query,params);
        } catch (NoDataFoundException e) {
            return null;
        }
    }

    /**
     * 
     * @param tr Transacción de trabajo
     * @param locId ID de la localidad
     * @param orgId ID del organismo
     * @param cuij CUIJ del expediente
     * @param sufijo Sufijo del expediente
     * @param numero Número del expediente
     * @param anio Año del expediente
     * @param bis Bis del expediente
     * @param clave Clave de ingreso del expediente
     * @return ID del expediente
     */
    public static Number getExpIdParte(DBTransaction tr, Number locId, 
                                       Number orgId, String cuij, 
                                       String sufijo, Number numero, 
                                       Number anio, String bis, String clave) throws Exception {
        if (UAssert.empty(cuij) && numero == null) {
            LOG.error(UMensajes.PARAMETRO_NULO, "cuij y numero");
            throw new USystemException("Los parámetros cuij y numero son nulos.");
        }
        if (UAssert.empty(clave)) {
            LOG.error(UMensajes.PARAMETRO_NULO, "clave");
            throw new USystemException("El parámetro clave es nulo.");
        }
        
        String claveEncriptada = UEncrypter.encripClaveExp(new Integer(clave));
        List<Object> params = UUtil.getList(claveEncriptada);
        String query = // req 1188
            "SELECT Expediente.EXP_ID \n" + "     FROM Expediente \n" + 
            "     INNER JOIN EXP_NUMERO ExpNumero ON (ExpNumero.EXP_ID = Expediente.EXP_ID AND ExpNumero.ESTADO = 0) \n" + 
            "     LEFT JOIN VISIBILIDAD_ORG_IURIX_IOL Visibilidad ON (Visibilidad.ORG_ID = Expediente.ORG_ID AND Visibilidad.ESTADO = 0) \n" + 
            "     LEFT JOIN Organismo ON (Organismo.ORG_ID = Expediente.ORG_ID AND Organismo.ESTADO = 0 AND Organismo.ORG_VISIBLE = 1) \n" + 
            "     LEFT JOIN Localidad ON (Localidad.LOC_ID = Expediente.LOC_ID AND Localidad.ESTADO = 0 AND Localidad.LOC_VISIBLE = 1) \n" + 
            "     LEFT JOIN Competencia ON (Competencia.COM_ID = Organismo.COM_ID AND Competencia.ESTADO = 0 AND Competencia.COM_VISIBLE = 1) \n" + 
            "     WHERE Expediente.EXP_CLAVE = ? \n" +
            "     AND Expediente.ESTADO = 0 \n";
        if (locId != null) { // req 3974
            query += "AND ExpNumero.LOC_ID = ? \n";
            params.add(locId);
        }
        if (orgId != null) {
            query += "AND ExpNumero.ORG_ID = ? \n";
            params.add(orgId);
        }
        if (UAssert.notEmpty(cuij)) {
            query += "AND Expediente.EXP_CUIJ = ? \n";
            params.add(cuij);
        }
        if (sufijo != null) {
            query += "AND Expediente.EXP_SUFIJO = ? \n";
            params.add(sufijo);
        } else {
            if (UAssert.notEmpty(cuij)) {
                query += "AND Expediente.EXP_SUFIJO IS NULL \n";
            }
        }
        if (numero != null) {
            query += "AND ExpNumero.NRO_NUMERO = ? \n";
            params.add(numero);
        }
        if (anio != null) {
            query += "AND ExpNumero.NRO_ANIO = ? \n";
            params.add(anio);
        }
        if (UAssert.notEmpty(bis)) {
            query += "AND ExpNumero.NRO_BIS = ? \n";
            params.add(bis);
        }

        try {
            return UConsultas.getValueAsNumber(tr, query, params);
        } catch (NoDataFoundException e) {
            return null;
        }
    }

    /**
     * 
     * @param tr Transacción de trabajo
     * @param orgId ID del organismo
     * @return Última fecha de proceso de IOL en el organismo
     */
    public static oracle.jbo.domain.Date getMaxProcFecha(DBTransaction tr, Number orgId) {
        if (UAssert.empty(orgId)) {
            LOG.error(UMensajes.PARAMETRO_NULO, "orgId");
            throw new USystemException("El parámetro orgId es nulo.");
        }
        // TODO ALE: sacar el TO_CHAR cuando las fechas en UConsultas no pierdan la hora
        String query = "SELECT TO_CHAR(MAX(PROC_FECHA),'DD/MM/YYYY HH24:MI') \n" +
            "FROM PROCESO_INTERNET ProcesoInternet \n" + 
            "WHERE ProcesoInternet.ORG_ID = ?";
        List<Object> params = UUtil.getList(orgId);
        try {
            String maxProcFecha = UConsultas.getValueAsString(tr, query, params);
            return Convert.toDate(maxProcFecha);
        } catch (NoDataFoundException e) {
            return null;
        }
    }

    /**
     * 
     * @param tr Transacción de trabajo.
     * @param logId ID del log.
     * @param proId ID del profesional.
     * @param logFecha Fecha del log.
     * @param logIp IP del usuario.
     * @param expId ID del expediente.
     * @param logExito Éxito del ingreso.
     * @param proCircunscripcion Circunscripción del profesional.
     * @param proColegio Colegio del profesional.
     * @param proMatricula Matrícula del profesional.
     * @param expCuij CUIJ del expediente.
     * @param nroNumero Número del expediente.
     * @param errorCaptcha True si el usuario falló el ingreso del captcha,
     * false si lo ingresó bien.
     * @param sessionId ID de la sesión HTTP del usuario.
     */
    public static void loguear(DBTransaction tr, Number logId, Number proId, 
        Date logFecha, String logIp, Number expId, String logExito,
        Number proCircunscripcion, Number proColegio, String proMatricula,
        String expCuij, Number nroNumero, Boolean errorCaptcha, String sessionId) {
        if (tr == null) {
            LOG.error(UMensajes.TRANSACCION_NO_DEFINIDA_LOG);
        }
        //MAS - GAB - PJSF01 - 11823 - 18/12/2014 - desde aca
        if (proMatricula != null && proMatricula.length() > 10)
           proMatricula = proMatricula.substring(0,9);
        //MAS - GAB - PJSF01 - 11823 - 18/12/2014 - HASTA ACA 
        String query = "INSERT INTO LOG ( \n" +
            "    LOG_ID, PRO_ID, LOG_FECHA, LOG_IP, EXP_ID, LOG_EXITO, \n" +
            "    PRO_CIRCUNSCRIPCION, PRO_COLEGIO, PRO_MATRICULA, EXP_CUIJ, \n" +
            "    NRO_NUMERO, LOG_ERROR_CAPTCHA, SESSION_ID \n" +
            ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
        
        List<Object> params = UUtil.getList(logId, proId, logFecha, logIp, 
            expId, logExito, proCircunscripcion, proColegio, proMatricula,
            expCuij, nroNumero, Convert.toNumber(errorCaptcha), sessionId);
        try {
            UConsultas.executeVoid(tr, query, params);
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    /**
     * 
     * @param tr Transacción de trabajo
     * @return Siguiente valor en la secuencia de log LOG_SEQ
     */
    public static Number getSequenceNextval(DBTransaction tr) {
        String query = "SELECT LOG_SEQ.NEXTVAL FROM DUAL";
        return UConsultas.getValueAsNumber(tr, query, null);
    }

    /**
     * 
     * @param carId ID del cargo
     * @return Datos del cargo.
     */
    public static  Map<String,Object> getCargo(Number carId) {
        if (carId == null) {
            LOG.error(UMensajes.PARAMETRO_NULO,"carId");
            throw new USystemException("El parámetro carId es nulo.");
        }
        
        String query = "SELECT Cargo.CAR_NUMERO, \n" + 
                "    Cargo.CAR_ANIO, \n" + 
                "    Cargo.CAR_PRESENTANTE \n" + 
                "    FROM Cargo \n" + 
                "    WHERE Cargo.CAR_ID = ?";

        List<Object> params = UUtil.getList(carId);
        try {
            return UConsultas.getValueAsMap(null,query,params);
        } catch (NoDataFoundException e) {
            return null;
        } 
    }  
    
    public static Map<String,Object> getFicha(Number expId) {
        if (expId == null) {
            LOG.error(UMensajes.PARAMETRO_NULO,"expId");
            throw new USystemException("El parámetro expId es nulo.");
        }
        
        String query = "SELECT Expediente.EXP_ID, \n" + 
            "       Expediente.ORG_ID, \n" + 
            "       ExpNumero.NRO_ID, \n" + 
            "       ExpNumero.EXP_CARATULA, \n" + 
            "       Expediente.EXP_CUIJ, \n" + 
            "       Expediente.EXP_SUFIJO, \n" + 
            "       ExpNumero.NRO_NUMERO, \n" + 
            "       ExpNumero.NRO_ANIO, \n" + 
            "       ExpNumero.NRO_BIS, \n" + 
            "       Organismo.ORG_NOMBRE, \n" + 
            "       Organismo.ORG_CODIGO, \n" + 
            "       Localidad.LOC_DESCR, \n" + 
            "       CASE \n" + 
            "           WHEN TO_CHAR(Expediente.EXP_FECHA_INICIO,'DD/MM/YYYY') = '01/01/1600' THEN \n" +
            "               TO_DATE(NULL,'DD/MM/YYYY') \n" +
            "           ELSE \n" +
            "               Expediente.EXP_FECHA_INICIO \n" + 
            "       END AS EXP_FECHA_INICIO, \n" + 
            "       Expediente.EXP_UBICACION, \n" + 
            "       TO_CHAR(Expediente.FECHA_ACTUALIZACION,'DD/MM/YYYY HH24:MI') AS FECHA_ACTUALIZACION, \n" + 
            "	'' AS nroExpediente, \n" + 
            "	'' AS cuijCompleto, \n" +
            "       Organismo.ORG_CANT_SEC, \n" +
            "       Secretaria.SEC_NOMBRE, \n" +
            "       Expediente.SEC_ID, \n" +
            "       Expediente.EXP_VISIBLE \n" +
            "       FROM Expediente \n" +
            "       LEFT JOIN EXP_NUMERO ExpNumero ON (Expediente.EXP_ID = ExpNumero.EXP_ID AND Expediente.ORG_ID = ExpNumero.ORG_ID) \n" +
            "       LEFT JOIN ORGANISMO Organismo ON (Expediente.ORG_ID = Organismo.ORG_ID) \n" +
            "       LEFT JOIN LOCALIDAD Localidad ON (Expediente.LOC_ID = Localidad.LOC_ID) \n" +
            "       LEFT JOIN SECRETARIA Secretaria ON (Organismo.ORG_ID = Secretaria.ORG_ID AND Expediente.SEC_ID = Secretaria.SEC_ID) \n" +
            "       WHERE Expediente.ESTADO = 0 \n" +
            "       AND Expediente.EXP_ID = ?";
        
        List<Object> params = UUtil.getList(expId);
        try {
            return UConsultas.getValueAsMap(null,query,params);
        } catch (NoDataFoundException e) {
            return null;
        } 
    }

    /**
     * 
     * @param tr Transacción de trabajo
     * @param actId ID de la actuación
     */
    public static BlobDomain getNovedadActTexto(DBTransaction tr, Number actId) {
        String query = "SELECT Actuacion.ACT_TEXTO \n" +
            "FROM Actuacion \n" +
            "WHERE Actuacion.ACT_ID = ?";
        List<Object> params = UUtil.getList(actId);
        try {
            return UConsultas.getValueAsBlob(tr,query,params);
        } catch (Exception e) {
            LOG.error(e.getMessage(),e);
            return null;
        }
    }

    /**
     *
     * @param tr Transacción de trabajo
     * @param orgId ID del organismo
     * @param secId ID de la secretaría
     * @return Equivalencia del organismo
     */
    public static Number getOrgCodigoEquivalente(DBTransaction tr, Number orgId, Number secId) {
        if (UAssert.empty(orgId)) {
            LOG.error(UMensajes.PARAMETRO_NULO,"orgId");
            return null;
        }
        if (UAssert.empty(secId)) {
            LOG.error(UMensajes.PARAMETRO_NULO,"secId");
            return null;
        }

        String query = "SELECT OrganismoEquiv.ORG_CODIGO_EQUIV \n" +
            "FROM ORGANISMO_EQUIV OrganismoEquiv \n" + 
            "WHERE OrganismoEquiv.ORG_ID = ? \n" + 
            "AND OrganismoEquiv.SEC_ID = ?\n" + 
            "AND ROWNUM = 1"; // por las dudas
        List<Object> params = UUtil.getList(orgId,secId);
        try {
            return UConsultas.getValueAsNumber(tr,query,params);
        } catch (NoDataFoundException e) {
            return null;
        }
    }

    /**
     * 
     * @param tr Transacción de trabajo
     * @param adjId ID del adjunto a cargo
     */
    public static BlobDomain getAdjuntoCargoArchivo(DBTransaction tr, Number adjId) {
        String query = "SELECT AdjuntoCargo.ADJ_ARCHIVO \n" +
            "FROM ADJUNTO_CARGO AdjuntoCargo \n" +
            "WHERE AdjuntoCargo.ADJ_ID = ?";
        List<Object> params = UUtil.getList(adjId);
        try {
            return UConsultas.getValueAsBlob(tr,query,params);
        } catch (Exception e) {
            LOG.error(e.getMessage(),e);
            return null;
        }
    }
    /**
     * @param tr Transacción de trabajo
     * @param locId ID de la secretaría
     */
    public static String getNombreLocalidad(DBTransaction tr, Number locId) {
        if (UAssert.empty(locId)) {
            LOG.error(UMensajes.PARAMETRO_NULO,"locId");
            return null;
        }

        String query = "SELECT LOC_DESCR FROM LOCALIDAD WHERE LOC_ID=?"; 
        List<Object> params = UUtil.getList(locId);
        try {
            return UConsultas.getValueAsString(tr,query,params);
        } catch (NoDataFoundException e) {
            return null;
        }
    }

    /**
     * @param tr Transacción de trabajo
     * @param orgId ID del organismo
     */
    public static String getNombreOrganismo(DBTransaction tr, Number orgId) {
        if (UAssert.empty(orgId)) {
            LOG.error(UMensajes.PARAMETRO_NULO,"orgId");
            return null;
        }

        String query = "SELECT ORG_NOMBRE FROM ORGANISMO WHERE ORG_ID=?"; 
        List<Object> params = UUtil.getList(orgId);
        try {
            return UConsultas.getValueAsString(tr,query,params);
        } catch (NoDataFoundException e) {
            return null;
        }
    }

    /**
     * 
     * @param tr Transacción de trabajo
     * @param carId ID del cargo
     * @return Nombre del organismo correspondiente al cargo.
     */
    public static String getNombreOrganismoCargo(DBTransaction tr, Number carId) {
        if (carId == null) {
            LOG.error(UMensajes.PARAMETRO_NULO,"carId");
            throw new USystemException("El parámetro carId es nulo.");
        }
        String query = "SELECT Organismo.ORG_NOMBRE \n" +
            "FROM Cargo \n" +
            "LEFT JOIN Organismo ON (Cargo.ORG_ID = Organismo.ORG_ID AND Organismo.ESTADO = 0) \n" +
            "LEFT JOIN VISIBILIDAD_ORG_IURIX_IOL VisibilidadOrgIurixIol ON (Cargo.ORG_ID = VisibilidadOrgIurixIol.ORG_ID AND VisibilidadOrgIurixIol.ESTADO = 0) \n" +
            "WHERE Cargo.ESTADO = 0 \n" +
            "AND Cargo.CAR_ID = ? \n" +
            "AND VisibilidadOrgIurixIol.VIS_CARGO = 1";
                        
        List<Object> params = UUtil.getList(carId);
        try {
            return UConsultas.getValueAsString(tr,query,params);
        } catch (NoDataFoundException e) {
            return null;
        }
    }

    /**
     * 
     * @param tr Transacción de trabajo
     * @param actId ID de la actuación
     * @return Nombre del organismo correspondiente a la actuación.
     */
    public static String getNombreOrganismoActuacion(DBTransaction tr, Number actId) {
        if (actId == null) {
            LOG.error(UMensajes.PARAMETRO_NULO,"actId");
            throw new USystemException("El parámetro actId es nulo.");
        }
        String query = "SELECT Organismo.ORG_NOMBRE \n" +
            "FROM Actuacion \n" +
            "LEFT JOIN Organismo ON (Actuacion.ORG_ID = ORGANISMO.ORG_ID)\n" +
            "LEFT JOIN VISIBILIDAD_ORG_IURIX_IOL VisibilidadOrgIurixIol ON (Actuacion.ORG_ID = VisibilidadOrgIurixIol.ORG_ID AND VisibilidadOrgIurixIol.ESTADO = 0) \n" +
            "WHERE Actuacion.ESTADO = 0 \n" +
            "AND Actuacion.ACT_ID = ? \n" +
            "AND ( \n" +
            "    Actuacion.ACT_TIPO = 0 AND VisibilidadOrgIurixIol.VIS_DECRETO = 1 \n" +
            "    OR ACTUACION.ACT_TIPO = 1 AND VisibilidadOrgIurixIol.VIS_AUTO = 1 \n" +
            "    OR ACTUACION.ACT_TIPO = 2 AND VisibilidadOrgIurixIol.VIS_SENTENCIA = 1 \n" +
            "    OR ACTUACION.ACT_TIPO = 3 AND VisibilidadOrgIurixIol.VIS_NOTIFICACION = 1" +
            ")";
                        
        List<Object> params = UUtil.getList(actId);
        try {
            return UConsultas.getValueAsString(tr,query,params);
        } catch (NoDataFoundException e) {
            return null;
        } 
    }
    
    /**
     * 
     * @param tr Transacción de trabajo
     * 
     */
    public static boolean existeNotificacionesFirmadasEnElDia(DBTransaction tr, Number proId) {
        String query = " SELECT 1 \n" +
            "\n FROM ACTUACION Actuacion \n" +
            "\n INNER JOIN Expediente ON (Actuacion.EXP_ID = Expediente.EXP_ID AND Expediente.ESTADO = 0)\n" + 
            "\n INNER JOIN Representante ON (Representante.EXP_ID = Expediente.EXP_ID AND Representante.ESTADO = 0)\n";
        if (proId != null){
            query += "\n WHERE Actuacion.ESTADO = 0 AND Actuacion.ACT_TIPO = 3 \n" +
            "\n AND TRUNC(Actuacion.ACT_FECFIR) = TRUNC(SYSDATE)  AND Representante.PRO_ID = ? AND rownum = 1 ";
        }else {
            query += "\nWHERE 1 = 0 "; 
        }

        List<Object> params = UUtil.getList(proId);
        LOG.info("Existe notificaciones FIRMADAS en el dia Query:\n{}\nParámetros:{}",query,UUtil.ver(params));
        return UConsultas.getExists(tr,query,params);
    }
    
    /**
     * Indica si el profesional tiene notificaciones con firma digital desde determinada fecha hasta hoy
     * @param tr Transacción de trabajo
     * 
     */
    public static boolean existeNotificacionesFirmadasDesdeElDia(DBTransaction tr, Number proId, Date fechaDesde) {
        String query = "\nSELECT 1 " +
        "\nFROM Representante " +
        "\nINNER JOIN Expediente ON (Representante.EXP_ID = Expediente.EXP_ID AND Expediente.ESTADO = 0)" +
        "\nINNER JOIN Actuacion ON (Actuacion.EXP_ID = Expediente.EXP_ID AND Actuacion.ESTADO = 0)" ;
        if (proId != null){
            query += "\nWHERE Representante.PRO_ID = ? AND Representante.ESTADO = 0 " +
                        "\nAND Actuacion.ACT_TIPO = 3  " +  //AND ACTUACION.ACT_TIPO_FIRMA=1
                        "\nAND TRUNC(Actuacion.ACT_FECFIR) >= ? AND rownum = 1 ";
        } else {
            query += "\nWHERE 1 = 0 ";
        }        
        List<Object> params = UUtil.getList(proId, UFechas.toDateFormat(fechaDesde));
        LOG.info("Existe notificaciones DESDE el dia Query:\n{}\nParámetros:{}",query,UUtil.ver(params));
        return UConsultas.getExists(tr,query,params);
    }

    /**
     * 
     * @param tr Transacción de trabajo
     * @return Siguiente valor en la secuencia de log LOG_SEQ
     */
    public static Number getSequenceNextval(DBTransaction tr, String name) {
        String query = "SELECT " + name +".NEXTVAL FROM DUAL";
        return UConsultas.getValueAsNumber(tr, query, null);
    }


    /**
     * 
     * @param tr Transacción de trabajo
     
     */
    public static Number getPrimerModeloNotificacion(DBTransaction tr, Number orgId) {
        String query = "SELECT Modelo.MOD_ID \n" +
            " FROM MODELO_NOTIFICACION Modelo \n" +
            " WHERE Modelo.MOD_ORG_ID = ? AND Modelo.estado = 0 AND rownum = 1 "+
            " ORDER BY MOD_NOMBRE ASC " ;
        List<Object> params = UUtil.getList(orgId);
        try {
            return UConsultas.getValueAsNumber(tr,query,params);
        } catch (Exception e) {
            LOG.error(e.getMessage(),e);
            return null;
        }
    }
    
    /**
     * 
     * @param tr Transacción de trabajo
     
     */
    public static BlobDomain getTextoModeloNotificacion(DBTransaction tr, Number modId) {
        String query = "SELECT Modelo.MOD_TEXTO \n" +
            " FROM MODELO_NOTIFICACION Modelo \n" +
            " WHERE Modelo.MOD_ID = ? AND Modelo.estado = 0 ";
            
        List<Object> params = UUtil.getList(modId);
        try {
            return UConsultas.getValueAsBlob(tr,query,params);
        } catch (Exception e) {
            LOG.error(e.getMessage(),e);
            return null;
        }
    }
    
    /**
     * 
     * @param tr Transacción de trabajo
     
     */
    public static BlobDomain getTextoNotificacion(DBTransaction tr, Number notId) {
        String query = "SELECT not.NOT_TEXTO \n" +
            " FROM NOTIFICACION_PROFESIONAL not \n" +
            " WHERE not.NOT_ID = ? ";
            
        List<Object> params = UUtil.getList(notId);
        try {
            return UConsultas.getValueAsBlob(tr,query,params);
        } catch (Exception e) {
            LOG.error(e.getMessage(),e);
            return null;
        }
    }

    /**
     * @param expId - Identificador de expediente ( EXP_ID )  
     * @param uuid - Universally unique identifier < UUID > -> Identificador único universal
     * @param remoteIp - Dirección IP que invocó el Librito. No necesariamente se ingresa con un Porfesional al sistema
     * Realiza la inserción en la tabla < TICKETS >
     * Mapea el expediente a un uuid para poder ser trasmitido vía la URL del Librito.
     * Del lado de la aplicación Flex: "tramix-ma-webapp", se lee dicho valor, 
     * se elimina esta tupla y se invoca el llamado al webservice "unitech-book-webapp"
     * ya con el expediente en cuestión.
     * 
     * Para no hacer uso de la trasnsacción / iterador del llamante, decidí crear una instancia del módulo localmente.-
     */
    public static void insertTicket( Number expId, String uuid, String remoteIp ) throws Exception {
        DBTransaction tr = null;
        CommonServiceImpl service = null;
        try {
            if( UAssert.empty( uuid) ) {
                LOG.error( UMensajes.PARAMETRO_NULO, "uuid" );
                throw new USystemException( "El parámetro uuid es nulo." );
            }
            if( UAssert.empty( expId) ) {
                LOG.error( UMensajes.PARAMETRO_NULO, "expId" );
                throw new USystemException( "El parámetro expId es nulo." );
            }
            if( UAssert.empty( remoteIp) ) {
                LOG.error( UMensajes.PARAMETRO_NULO, "remoteIp" );
                throw new USystemException( "El parámetro remoteIp es nulo." );
            }
            
            service = UService.createCommonModule();
            tr = service.getDBTransaction();
    
            String query = 
                "INSERT INTO TICKETS\n" + 
                "  ( TICKET_ID, EXP_ID, UUID, AUD_IP_ALTA, AUD_FEC_ALTA )\n" + 
                "VALUES\n" + 
                "  ( TICKET_SEQ.NEXTVAL, ?, ?, ?, SYSDATE )";
         
            List<Object> params = UUtil.getList( expId, uuid, remoteIp );

            UConsultas.executeVoid( tr, query, params );
            tr.commit();
            
        } catch( Exception e ) {
            throw e;
        } finally {
            if( service != null ) {
                UService.releaseModule( service );
            }
        }       
    }

    /**
     * @param tr Transacción de trabajo.
     * @param orgId ID del organismo del expediente.
     * @return Datos de visibilidad del organismo para la ficha de un expediente
     * (visibilidad de adjuntos y visibilidad del botón de gestión de expedientes).
     */
    public static Map<String, Object> getVisibilidadOrganismo(DBTransaction tr, Number orgId) {
        if (orgId == null) {
            LOG.error(UMensajes.PARAMETRO_NULO,"orgId");
            throw new USystemException("El parámetro orgId es nulo.");
        }
    
        String query = "SELECT VISIBILIDAD_ORG_IURIX_IOL.VIS_ADJUNTO," +
            "       VISIBILIDAD_ORG_IURIX_IOL.VIS_BTN_NOTIF \n" +
            "FROM VISIBILIDAD_ORG_IURIX_IOL \n" +
            "WHERE VISIBILIDAD_ORG_IURIX_IOL.ORG_ID = ?";
        List<Object> params = UUtil.getList(orgId);
        try {
            return UConsultas.getValueAsMap(tr,query,params);
        } catch (NoDataFoundException e) {
            return null;
        }
    }
    
    public static List< Map< String, Object > > getTextosCargoActuacion( DBTransaction tr, Number expId ) {
        String query =
            "(\n" + 
            "  SELECT \n" + 
            "    A.ROWID AS CLAVE,\n" + 
            "    'A' AS TIPO,\n" + 
            "    A.ACT_ID AS IDENTIFICADOR,\n" + 
            "    A.ACT_TEXTO AS TEXTO,\n" + 
            "    A.ACT_FECFIR AS FECHA\n" + 
            "  FROM ACTUACION A\n" + 
            "  WHERE A.ESTADO = 0 \n" + 
            "  AND A.EXP_ID = ? \n" + 
            "  AND A.ACT_TEXTO IS NOT NULL \n " +
            ")\n" + 
            "UNION ALL\n" + 
            "(\n" + 
            "  SELECT\n" + 
            "    C.ROWID AS CLAVE,\n" + 
            "    'C' AS TIPO,\n" + 
            "    AC.CAR_ID AS IDENTIFICADOR,\n" + 
            "    AC.ADJ_ARCHIVO AS TEXTO,\n" + 
            "    AC.ADJ_FECHA AS FECHA\n" + 
            "  FROM ADJUNTO_CARGO AC\n" + 
            "  INNER JOIN CARGO C ON( C.CAR_ID = AC.CAR_ID )\n" + 
            "  WHERE AC.ESTADO = 0 \n" + 
            "  AND C.EXP_ID = ? \n" + 
            "  AND AC.ADJ_ARCHIVO IS NOT NULL \n" +
            "  AND AC.ADJ_EXTENSION = 'PDF' \n" + // Busca solo Adjuntos de Cargos con extensión .PDF.-
            ")\n" + 
            "ORDER BY FECHA ";
        List<Object> params = UUtil.getList( expId, expId );
        try {
            return UConsultas.getValues( tr, query, params );
        } catch( NoDataFoundException e ) {
            return null;
        } catch( SQLException e ) {
            LOG.error( "Error en: < ExpedienteUtil.getTextosCargoActuacion( ... ) >" );
            return null;
        }
    }
    
    
    /**
     * @param tr Transacción de trabajo.
     * @param idMagistrado ID del magistrado.
     */
    public static String getNombreMagistrado(DBTransaction tr, Number idMagistrado) {
        if (UAssert.empty(idMagistrado)) {
            LOG.error(UMensajes.PARAMETRO_NULO,"idMagistrado");
            return null;
        }

        String query = " SELECT USIOL_NOMBRE_APE FROM usuario_iol WHERE USIOL_ID=? "; 
        List<Object> params = UUtil.getList(idMagistrado);
        try {
            return UConsultas.getValueAsString(tr,query,params);
        } catch (NoDataFoundException e) {
            return null;
        }
    }
    
    //PJSF01 - 12638 - GNE - 1/12/2015 - desde aca
    public static Number getNextvalSequence(DBTransaction tr, String sequence) {
        String query = "SELECT " + sequence + ".NEXTVAL FROM DUAL";
        return UConsultas.getValueAsNumber(tr, query, null);
    }
    
    public static String getTamanio(double tam){
        double kb = 1024;
        double tamanio = Math.round((tam/kb)*100)/100.0d;
        return (new Double(tamanio)).toString();
    }
    
    public static void loguearDescarga(DBTransaction tr, Number desId,
            Number expId, Number actId, Number carId, Number adjId,
            Number proId, String tamanio, String ip, String sessionId) {
        if (tr == null) {
            LOG.error(UMensajes.TRANSACCION_NO_DEFINIDA_LOG);
        }
    
        String query = "INSERT INTO DESCARGA_IOL \n" +
                       "(DES_ID, DES_IP, EXP_ID, ACT_ID, CAR_ID, ADJ_ID, PRO_ID, \n" +
                       "DES_TAMANIO, DES_FECHA, SESSION_ID) \n" +
                       "VALUES (?,?,?,?,?,?,?,?,?,?)";
        
        List<Object> params = UUtil.getList(desId, ip, expId, actId, carId,
            adjId, proId, tamanio, UUtil.getCurrentDateAndTime(), sessionId);
        try {
            UConsultas.executeVoid(tr, query, params);
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        }
    }
    //PJSF01 - 12638 - GNE - 1/12/2015 - hasta aca

     /**
      * 
      * @param tr Transacción de trabajo
      * 
      */
     public static boolean existeExpienteVisible(DBTransaction tr, Number expId) {
         String query = " SELECT 1 \n" +
             " FROM EXPEDIENTE Expediente \n" +
             " WHERE Expediente.EXP_ID = ? AND EXP_VISIBLE = 'S' AND rownum = 1 ";
         
         List<Object> params = UUtil.getList(expId);
         return UConsultas.getExists(tr,query,params);
     }
     
    /**
     * 
     * @param expId ID del expediente
     * @return Datos del ingreso de las notificaciones para el organismo del expediente pasado como parámetro.
     */
    public static  Map<String,Object> getIngresoNotificaciones(Number expId) {
        if (expId == null) {
            LOG.error(UMensajes.PARAMETRO_NULO,"expId");
            throw new USystemException("El parámetro expId es nulo.");
        }
        
        String query = "SELECT Visibilidad.VIS_NOT_INGRESO_DESDE_1,\n" + 
        "      Visibilidad.VIS_NOT_INGRESO_HASTA_1,\n" + 
        "      Visibilidad.VIS_NOT_INGRESO_DESDE_2,\n" + 
        "      Visibilidad.VIS_NOT_INGRESO_HASTA_2\n" + 
        "FROM Expediente\n" + 
        "      LEFT JOIN VISIBILIDAD_ORG_IURIX_IOL Visibilidad ON (Visibilidad.ORG_ID = Expediente.ORG_ID AND Visibilidad.ESTADO = 0) \n" + 
        "WHERE Expediente.EXP_id = ? \n" + 
        "   AND Expediente.ESTADO = 0 ";

        List<Object> params = UUtil.getList(expId);
        try {
            return UConsultas.getValueAsMap(null,query,params);
        } catch (NoDataFoundException e) {
            return null;
        } 
    }
}
