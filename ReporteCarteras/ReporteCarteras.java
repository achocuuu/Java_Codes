/**
 * ***********************************************************************
 * Autor   : Jaime Ignacio Valenzuela - VMetrix International Spa.
 * Version : v1.0
 * Fecha   : 17/11/2022.
 * Proyecto: Pensiones Profuturo - ReporteCarteras
 * 
 *  Date   				    :  	05/01/2023
 *	Version/Author          :  	v2.0 / Jaime Ignacio Valenzuela 
 *	Description     	    :   JVG-20230105: Integración de venta por PEPS
 * 
 *  Date   				    :  	15/02/2023
 *	Version/Author          :  	v2.1 / Jaime Ignacio Valenzuela 
 *	Description     	    :   JVG-20230215: Correccion en computos de titulos usando UDI Proyectada. 
 * 
 *  Date   				    :  	27/02/2023
 *	Version/Author          :  	v2.2 / Jaime Ignacio Valenzuela 
 *	Description     	    :   JVG-27022023: Nombre de portafolio e interes cupon por posicion.  
 *
 *  Date   				    :  	10/03/2023
 *	Version/Author          :  	v2.2 / Jaime Ignacio Valenzuela 
 *	Description     	    :   JVG-10032023: Corrección Bug de 2 listas a una mismo objeto
 * 
 */

package com.vmetrix.v3.custom.proyector.ReporteCarteras;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.Optional;
import java.util.Map;

import cl.vmetrix.core.dto.DTO;
import cl.vmetrix.persistencelayer.database.DatabaseBO;
import cl.vmetrix.persistencelayer.database.DatabaseBOException;
import cl.vmetrix.vaadin.components.popup.VMetrixPopup;
import cl.vmetrix.vcube.environment.VCubeSystem;
import cl.vmetrix.vcube.environment.VCubeSystemException;
import cl.vmetrix.vcube.vcode.display.IDisplayReference;
import cl.vmetrix.vcube.vcode.types.IVCodeWindow;
import cl.vmetrix.vcube.vcode.types.VCodeException;
import cl.vmetrix.vcube.vcode.types.windowCode.VLayout;
import cl.vmetrix.vcube.vcode.types.windowCode.VLayout.Orientation;
import cl.vmetrix.vcube.vcode.types.windowCode.components.VButton;
import cl.vmetrix.vcube.vcode.types.windowCode.components.VDatePicker;
import cl.vmetrix.vcube.vcode.types.windowCode.components.VTextField;
import cl.vmetrix.vcube.vcode.types.windowCode.components.VGrid;
import cl.vmetrix.vcube.vcode.types.windowCode.components.VGrid.SelectionMode;
import cl.vmetrix.vcube.vcode.types.windowCode.components.VLabel;
import com.vmetrix.v3.custom.EnumPensiones.enums.EnumsPensionesUserTranDef;
import com.vmetrix.v3.custom.EnumPensiones.enums.EnumsPensionesAcctResult;
import com.vmetrix.v3.custom.EnumPensiones.enums.EnumsPensionesPortfolios;
import com.vmetrix.v3.custom.commons.db.ColumnType;
import com.vmetrix.v3.custom.commons.db.Parameter;
import com.vmetrix.v3.custom.commons.db.Query;


public class ReporteCarteras implements IVCodeWindow{
	

	static Logger logger = Logger.getLogger(ReporteCarteras.class.getSimpleName());
	
	// Basic Variables
	@SuppressWarnings("unused")
	private IDisplayReference display;
	private DatabaseBO db = DatabaseBO.getMainInstance();		
	private LocalDate systemDate;
	
	//Dates
	private VDatePicker dProcessDate		= new VDatePicker("Process Date");
	//Int para proyección
	private VTextField k_proy = new VTextField("Cantidad de Fechas de Proyeccion en Meses");
	
	// Grids
	VGrid gridReporteCarteras			= new VGrid("Reporte Carteras");
	
	// Components
	VLabel lblMainTitle			= new VLabel("Reporte Carteras");
	VButton btnGetValues		= new VButton("Get Values");

			
			
	@Override
	public VLayout getVisualComponent() throws VCodeException
	{
		
	// Main Layouts
		VLayout layoutMain		= VLayout.getInstance( Orientation.VERTICAL );
		VLayout layoutTop		= VLayout.getInstance( Orientation.HORIZONTAL );
		VLayout layoutContent	= VLayout.getInstance( Orientation.VERTICAL );
		VLayout layoutGrille	= VLayout.getInstance( Orientation.VERTICAL );
		VLayout layoutChart		= VLayout.getInstance( Orientation.VERTICAL );
		
	// Layout Width
		layoutMain.setWidth("100%");
		layoutContent.setWidth("100%");
		layoutGrille.setWidth("100%");
		layoutChart.setWidth("100%");
		
		
	// Set Styles
		lblMainTitle		.setStyle( "center subtitle-XXL" );
		btnGetValues		.setStyle( "tiny primary blue fa-calculator iconLeft" );

	// Layout Styles
		layoutTop.setStyle("horizontal left bottom");

	// Set Initial Values

		//Grid
		gridReporteCarteras.setSelectionMode(SelectionMode.SINGLE);
	
		
		//Dates
		try {
			
			systemDate = VCubeSystem.getSystemDate();
			dProcessDate.setValue(systemDate);
			k_proy.setValue("1");
			//Botton K Begin
			
			int iError = 0;
			String sError = "";
			
			if(k_proy.getValue() == null || k_proy.getValue() == "0"){
				iError++;
				sError = "El valor de proyeccion debe ser mayor a 1.";
			}

			if(iError > 0){
				VMetrixPopup.createWarning()
				.withMessage(sError)
				.withOkButton()
				.open();
			}
			//Botton K End
			
			
		} catch (VCubeSystemException e) {
			e.printStackTrace();
		}

		
		// Setting button Actions
		
		//Calibrate Button
		btnGetValues.setClickAction(()->{
			
			try
			{
				//Begin
				setView();



			}catch(Exception e)
			{
				VMetrixPopup.createError()
				.withMessage(e.getMessage())
				.withOkButton()
				.open();
			}

		    
		});
		
		
	
		
		// Main Structure
		layoutTop.addComponents(dProcessDate, k_proy, btnGetValues);
		
		layoutGrille.addComponents(gridReporteCarteras);

		layoutContent.addComponents(layoutChart,layoutGrille);
		
		
		//Setting Main LayOut
		layoutMain.addComponents(lblMainTitle,layoutTop,layoutContent);
		
		return layoutMain;
		
	}

	@Override
	public void setDisplayReference(IDisplayReference arg0) {
		this.display = arg0;
	}
	

	
	
	
	private void setView()
	{
		
		//Setting Reporte Cartera
		List<HashMap<String, Object>> listOutput = null;
		List<DTO> listOutputDTO = getGridReporteCarteras();
		
		ventasPEPS();
		//Agregar la tabla del blotter
		List<DTO> listOutputCarteraBlotterDTO = getGridReporteCarterasBlotter();
		List<DTO> listOutputCarteraBlotterVentasDTO = getGridReporteCarterasBlotterVentas();
		//Si existe la tabla de blotter que se incluya en el reporte
		if(listOutputCarteraBlotterDTO != null)
			listOutputDTO.addAll(listOutputCarteraBlotterDTO);
		if(listOutputCarteraBlotterVentasDTO != null)
			listOutputDTO.addAll(listOutputCarteraBlotterVentasDTO);
		
		
//		//Agregar la tabla del supuestos egresos e ingresos
//		List<DTO> listOutputCarteraSupEIDTO = getGridReporteCarterasSupEI();
//		
//		//Si existe la tabla de blotter que se incluya en el reporte
//		if(listOutputCarteraSupEIDTO != null)
//			listOutputDTO.addAll(listOutputCarteraSupEIDTO);
		
		
		List<DTO> finalResult = getGroupedResults(listOutputDTO);
		
//		listOutput = listOutputDTO
//				.stream().map(dto -> dto.getHashMap())
//				.collect(Collectors.toList());

		listOutput = finalResult
		.stream().map(dto -> dto.getHashMap())
		.collect(Collectors.toList());

		
		//Setting Grille
		gridReporteCarteras.setItems(listOutput); 
		
		
		
	}
	
	

	/**
	 * Carga Grilla 
	 * @return 
	 */
	private List<DTO> getGridReporteCarteras(){
		List<DTO> listOut = null;

		listOut = execQuery(getReporteCarteras());
	
		return listOut;
		
	}
	
	
	private List<DTO> getGridReporteCarterasBlotter(){
		List<DTO> listOut = null;

		listOut = execQuery(getReporteCarterasBlotter());
	
		return listOut;
		
	}
	
	private List<DTO> getGridReporteCarterasSupEI(){
		List<DTO> listOut = null;

		listOut = execQuery(getReporteCarterasSupEI());
	
		return listOut;
		
	}
	
	private List<DTO> getGridReporteCarterasBlotterVentas(){
		List<DTO> listOut = null;

		listOut = execQuery(getVentasBlotterProyection());
	
		return listOut;
		
	}
	private List<DTO> execQuery(String sQuery){
		
		List<DTO> results = null;
		
		logger.log(Level.INFO,sQuery);

		try {
			
			results = db.executeQuery(sQuery, null );
		
		} catch (DatabaseBOException e) {
			logger.log(Level.WARNING, e.getMessage());	
			//VMetrixPopup.createWarning().withLongMessage(e.getMessage()).withOkButton().open();

		}
		
		if (results.size() <= 0 ) {
			logger.log(Level.INFO, "execQuery: There are not result for this Query \n\n " + sQuery);

		}
		
		return results;
	}
	// JVG-20230105: Algoritmo de venta por PEPS
	private void ventasPEPS(){
		// [JVG]: Obtiene los datos 
			List<DTO> blotter_ventas = execQuery(getBlotterVentas());
			List<DTO> ventas_transacctions_PEPS = execQuery(getVentasTransacctionsPEPS());
			List<DTO> resultado_proyeccion_ventas = new ArrayList<DTO>();

			List<DTO> ventas_ticker = new ArrayList<DTO>();
			// List<DTO> ventas_ticker_Ant = new ArrayList<DTO>(); //JVG-10032023: SE COMENTA YA QUE SE ITERA DENTRO DEL LOOP
	        for (int i = 0; i < blotter_ventas.size(); i ++){
	
	            DTO elementoAnt_Blotter = i == 0 ? null : blotter_ventas.get(i - 1);
	            DTO elemento_Blotter = blotter_ventas.get(i);
	         
	            String ticker = elemento_Blotter.getValue("TICKER").toString();
	            String proj_date = elemento_Blotter.getValue("PROJ_DATE").toString();
	            String ticker_anterior = i == 0 ? "" :getValueStr(elementoAnt_Blotter, "TICKER");
	            double tran_position_sell = getValueDbl(elemento_Blotter,"TRAN_POSITION");
	           if (ticker.equals(ticker_anterior)){ //[JVG]: En caso de que pasamos a otro proj_date del mismo ticker, quedarse con la selección anterior
	
	        	 //JVG-10032023: MAPPEAR A VENTAS_TICKER
	               ventas_ticker = ventas_ticker.stream().filter(dto-> !dto.getValue("TRAN_POSITION").equals(0.0)).collect(Collectors.toList());
	            }else{
	            	// [JVG]: Obtiene las transacciones a las cuales descontar por las ventas
	                ventas_ticker = ventas_transacctions_PEPS.stream().filter(dto-> dto.getValue("TICKER").equals(ticker)).collect(Collectors.toList());
	
	            }
	
	            double remanente = 0.0;
	            double new_position = 0.0;
	            double position_cumulative = 0.0;
	          //JVG-10032023: CREAR LISTA ACA Y QUE PARA CADA ITERACION SE GUARDE COMO NUEVO OBJETO
	            List<DTO> ventas_ticker_Ant = new ArrayList<DTO>();
	           for (int j = 0; j < ventas_ticker.size(); j++){
	
	               DTO elemento = ventas_ticker.get(j);
	            // [JVG]: Obtiene la nueva posicion para proyectar y que se cumpla la condicion de acumulación, obteniendo remanente hasta que se acaben las ventas. 
	               if (tran_position_sell != position_cumulative) {
	
	                   double tran_position = getValueDbl(elemento, "TRAN_POSITION");
	                   remanente = j == 0 ? tran_position + tran_position_sell : tran_position + remanente;
	                   new_position = remanente < 0 ? -tran_position : remanente - tran_position;
	                   double final_position = new_position + tran_position;
	                   position_cumulative = position_cumulative + new_position;
	
	                   elemento.setValue("REMANENTE", remanente);
	                   elemento.setValue("NEW_POSITION", new_position);
	                   elemento.setValue("POSITION_FINAL", final_position);
	                   elemento.setValue("TRAN_POSITION", final_position); //[JVG]: Se reemplaza por el new tran position
	                  
	                   elemento.setValue("PROJ_DATE", proj_date);
	
	               }else {
	
	                   elemento.setValue("REMANENTE", 0.0);
	                   elemento.setValue("NEW_POSITION", 0.0);
	                   elemento.setValue("POSITION_FINAL", 0.0);	        
	                   elemento.setValue("PROJ_DATE", proj_date);
	
	               }
	
	                //[JVG]: Guardar resultado de new position para el proj date
	             //JVG-10032023: CREAR NUEVO DTO PARA QUE LAS LISTAS NO SE REFERENCIEN AL MISMO OBJETO
	               DTO dtoNew = new DTO();
	               dtoNew.setValue("PROCESS_DATE", elemento.getValue("PROCESS_DATE"));
	               dtoNew.setValue("TRAN_DEAL", elemento.getValue("TRAN_DEAL"));
	               dtoNew.setValue("PORT_ID", elemento.getValue("PORT_ID"));
	               dtoNew.setValue("INS_ID", elemento.getValue("INS_ID"));
	               dtoNew.setValue("TICKER", elemento.getValue("TICKER"));
	               dtoNew.setValue("TRAN_RATE", elemento.getValue("TRAN_RATE"));
	               dtoNew.setValue("CLAS_CONTABLE", elemento.getValue("CLAS_CONTABLE"));
	               dtoNew.setValue("CLAS_RIESGO", elemento.getValue("CLAS_RIESGO"));
	               dtoNew.setValue("RSCH_ID", elemento.getValue("RSCH_ID"));
	               dtoNew.setValue("TRAN_POSITION", elemento.getValue("TRAN_POSITION"));
	               dtoNew.setValue("TRAN_SETTLE_DATE", elemento.getValue("TRAN_SETTLE_DATE"));
	               dtoNew.setValue("TRAN_TRADE_DATE", elemento.getValue("TRAN_TRADE_DATE"));
	               dtoNew.setValue("TICKER_BLOTTER", elemento.getValue("TICKER_BLOTTER"));
	               dtoNew.setValue("REMANENTE", elemento.getValue("REMANENTE"));
	               dtoNew.setValue("NEW_POSITION", elemento.getValue("NEW_POSITION"));
	               dtoNew.setValue("POSITION_FINAL", elemento.getValue("POSITION_FINAL"));
	               dtoNew.setValue("PROJ_DATE", elemento.getValue("PROJ_DATE"));
	               ventas_ticker_Ant.add(dtoNew);
	
	           }
	            //ventas_ticker_Ant = ventas_ticker;
	            resultado_proyeccion_ventas.addAll(ventas_ticker_Ant.stream().filter(dto-> !dto.getValue("REMANENTE").equals(0.0)).collect(Collectors.toList()));
	
	
	        }
	
	        //[JVG]: Guarda los resultados en una tabla
	        saveResults(resultado_proyeccion_ventas);
		
		
	}
	
	private String getReporteCarteras()
	{
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("\n  /*-- CTE WITH PARAMETERS FROM JAVA */");
		sb.append("\n  WITH PARAMETERS AS (");
		sb.append("\n      SELECT");
		sb.append("\n          TO_DATE('" + dProcessDate.getValue().toString() + "','YYYY-MM-DD')  AS PROCESS_DATE");
		sb.append("\n          ,TO_DATE('" + dProcessDate.getValue().toString() + "','YYYY-MM-DD')  AS PROJ_DATE");
		sb.append("\n      FROM DUAL	),");
		sb.append("\n /*--PERIOD DATES*/");
		sb.append("\n  P_DATES AS (");
		sb.append("\n  SELECT ");
		sb.append("\n  	PERIOD_DATE ");
		sb.append("\n  FROM TABLE(VCUBE_USER.PENS.TVF_PROJ_PERIOD(TO_DATE('" + dProcessDate.getValue().toString() + "','YYYY-MM-DD') ))");
		sb.append("\n  fetch first " + k_proy.getValue().toString() + " rows only");
		sb.append("\n  ), /*FECHAS DE CARTERA VER COMO DETERMINAR ESTO PONER UNA VARIABLE GLOBAL*/");
		sb.append("\n  ");
		sb.append("\n   /*--obtener los tran info de clasificacion contable y de riesgo */");
		sb.append("\n  CLASIFICACIONES AS(   ");
		sb.append("\n SELECT ");
		sb.append("\n 	A.PROCESS_DATE ");
		sb.append("\n 	,A.TRAN_ID");
		sb.append("\n 	,A.UTD_VALUE CLAS_CONTABLE");
		sb.append("\n 	,B.UTD_VALUE CLAS_RIESGO");
		sb.append("\n FROM VCUBE.USER_TRAN_DEF_STRING A ");
		sb.append("\n            LEFT JOIN VCUBE.USER_TRAN_DEF_STRING B ON (A.TRAN_ID = B.TRAN_ID AND A.PROCESS_DATE = B.PROCESS_DATE  AND B.UTD_ID = "+EnumsPensionesUserTranDef.UTD_PENS_TRAN_CLASIF_RIESGO.toInt()+" )");
		sb.append("\n WHERE A.UTD_ID = "+EnumsPensionesUserTranDef.UTD_PENS_TRAN_CLASIF_CONTABLE.toInt()+"");
		sb.append("\n AND A.PROCESS_DATE = TO_DATE('" + dProcessDate.getValue().toString() + "','YYYY-MM-DD') ");
		sb.append("\n ),");
		sb.append("\n  /*-- cte base con todas las transacciones consideradas dentro del calculo */");
		sb.append("\n BASE_TRANSACTIONS AS (              ");
		sb.append("\n SELECT");
		sb.append("\n  T.PROCESS_DATE AS PROCESS_DATE");
		sb.append("\n  ,T.TRAN_DEAL");
		sb.append("\n  ,T.PORT_ID");
		sb.append("\n  ,C.CCY_NAME ");
		sb.append("\n  ,I.INS_ID");
		sb.append("\n  ,ID.INSD_TICKER INSTRUMENTO");
		sb.append("\n  ,VV.TV AS TIPO_VALOR");
		sb.append("\n  ,VV.EMISORA ");
		sb.append("\n  ,VV.SERIE ");
		sb.append("\n  , TTYPE.TRANT_NAME AS T");
		sb.append("\n  , T.TRAN_TRADE_DATE AS F_ULT_COMPRA");
		sb.append("\n  , T.PROCESS_DATE + VV.DIAS_VENC AS F_VTO");
		sb.append("\n  , VV.PLAZO ");
		sb.append("\n  , VV.DIAS_VENC DXV");
		sb.append("\n  , YIELD.UTD_VALUE/100.00 AS TASA_COMPRA");
		sb.append("\n  , CLAS.CLAS_CONTABLE");
		sb.append("\n  ,T.TRAN_CLOSE_POS AS TRAN_POSITION ");
		sb.append("\n  , CASE ");
		sb.append("\n  	WHEN CLAS.CLAS_CONTABLE = 'A Financiar' THEN VV.RENDIMIENTO/100.00 ELSE YIELD.UTD_VALUE/100.00 ");
		sb.append("\n   END AS TRAN_RATE");
		sb.append("\n , VV.INTERES_DEV  AS INTERES_CPN /*ES EL VALOR EN T0*/");
		sb.append("\n  , VV.TASA_CUPON_VIGENTE AS TASA_CUPON ");
		sb.append("\n  , VV.FECHA_FIN_CUPON   ");
		sb.append("\n  , VV.FECHA_INICIO_CUPON ");
		sb.append("\n  , CASE WHEN CLAS.CLAS_RIESGO = 'Reservas' THEN T.TRAN_CLOSE_POS ELSE 0.0 END AS TIT_TECNICAS");
		sb.append("\n  , CASE WHEN CLAS.CLAS_RIESGO = 'Capital' THEN T.TRAN_CLOSE_POS ELSE 0.0 END AS TIT_CAPITAL");
		sb.append("\n  , CASE WHEN CLAS.CLAS_RIESGO = 'Otros Pasivos' THEN T.TRAN_CLOSE_POS ELSE 0.0 END AS TIT_OTROS");
		sb.append("\n  ,T.TRAN_SETTLE_DATE /*PARA VALORIZAR*/");
		sb.append("\n  , VV.PRECIO_SUCIO AS PS_VALMER");
		sb.append("\n  , MON.PRECIO_SUCIO AS DIVISA ");
		sb.append("\n      FROM VCUBE.TRANSACTION T");
		sb.append("\n          LEFT JOIN PARAMETERS PARAM ON ( 1 = 1 )");
		sb.append("\n          LEFT JOIN VCUBE.INSTRUMENT I ON ( I.INS_ID = T.INS_ID  AND I.PROCESS_DATE = T.PROCESS_DATE  )");
		sb.append("\n          LEFT JOIN VCUBE.INS_DETAIL ID ON ( ID.INS_ID = T.INS_ID AND ID.PROCESS_DATE = T.PROCESS_DATE  )");
		sb.append("\n          LEFT JOIN VCUBE.USER_TRAN_DEF_DOUBLE YIELD ON ( T.TRAN_ID = YIELD.TRAN_ID AND T.PROCESS_DATE = YIELD.PROCESS_DATE )");
		sb.append("\n          LEFT JOIN VCUBE.USER_TRAN_DEF_DATE DT ON ( T.TRAN_ID = DT.TRAN_ID AND T.PROCESS_DATE = DT.PROCESS_DATE AND DT.UTD_ID  = "+EnumsPensionesUserTranDef.UTD_PENS_TRAN_FECHA_RESOLUCION.toInt()+")");
		sb.append("\n          LEFT JOIN VCUBE.BUY_SELL BS ON ( BS.BS_ID  = T.BS_ID )");
		sb.append("\n          LEFT JOIN CLASIFICACIONES CLAS ON (T.TRAN_ID = CLAS.TRAN_ID AND T.PROCESS_DATE = CLAS.PROCESS_DATE )");
		sb.append("\n          LEFT JOIN VCUBE_USER.VECTOR_VALMER VV ON (ID.INSD_TICKER = VV.INSTRUMENTO AND T.PROCESS_DATE = VV.PROCESS_DATE )");
		sb.append("\n       	 LEFT JOIN VCUBE.CURRENCY C ON (I.BASE_CCY_ID = C.CCY_ID)");
		sb.append("\n       	 LEFT JOIN VCUBE.TRAN_TYPE TTYPE ON (TTYPE.TRANT_ID = T.TRANT_ID)");
		sb.append("\n       	 LEFT JOIN VCUBE_USER.VECTOR_VALMER MON ON (T.PROCESS_DATE = MON.PROCESS_DATE AND MON.INSTRUMENTO = concat(concat(concat('*C_MXP', C.CCY_NAME), '_'), C.CCY_NAME))");
		sb.append("\n      WHERE");
		sb.append("\n          T.PROCESS_DATE = PARAM.PROCESS_DATE");
		sb.append("\n          AND I.PROD_ID = 5");
		sb.append("\n          AND T.ASTT_ID = 2");
		sb.append("\n          AND YIELD.UTD_ID = "+EnumsPensionesUserTranDef.UTD_PRICING_YIELD.toInt()+"");
		sb.append("\n          AND T.PORT_ID IN ("+EnumsPensionesPortfolios.IMSS.toInt()+","+EnumsPensionesPortfolios.ISSSTE.toInt()+","+EnumsPensionesPortfolios.PORVENIR.toInt()+")");
		sb.append("\n          AND BS.BS_ID = 0");
		sb.append("\n 		 AND ID.INSD_TICKER NOT IN ('97_MXMACFW_07-4U')" );
		sb.append("\n ),");
		sb.append("\n ");
		sb.append("\n /* CTE QUE OBTIENE LOS DIFERENTES FLUJOS PARA CADA INSTRUMENTO */");
		sb.append("\n  CASHFLOWS AS (");
		sb.append("\n  SELECT");
		sb.append("\n      T.*");
		sb.append("\n      ,INTR.INT_FLOW");
		sb.append("\n      ,INTR.INT_START_DATE");
		sb.append("\n      ,INTR.INT_END_DATE");
		sb.append("\n      ,INTR.INT_PAYMENT_DATE");
		sb.append("\n     -- ,LAST_DAY(INTR.INT_END_DATE) P_DATE");
		sb.append("\n      ,INTR.INT_AMOUNT + NVL(CF.CF_AMOUNT, 0) TOTAL_AMOUNT");
		sb.append("\n      ,YC.YLDCMP_NAME");
		sb.append("\n      ,DCC.DCC_NAME ");
		sb.append("\n      ,INTR.INT_NOTIONAL ");
		sb.append("\n  FROM BASE_TRANSACTIONS T");
		sb.append("\n      LEFT JOIN PARAMETERS PARAM ON ( 1 = 1 )");
		sb.append("\n      LEFT JOIN VCUBE.INSTRUMENT I ON (I.INS_ID = T.INS_ID AND I.PROCESS_DATE= T.PROCESS_DATE)");
		sb.append("\n      LEFT JOIN VCUBE.INS_DETAIL ID ON (I.INS_ID = ID.INS_ID AND ID.PROCESS_DATE = T.PROCESS_DATE)");
		sb.append("\n      LEFT JOIN VCUBE.CURRENCY C ON (I.BASE_CCY_ID = C.CCY_ID)");
		sb.append("\n      LEFT JOIN VCUBE.INTEREST INTR ON (I.INS_ID = INTR.INS_ID AND INTR.PROCESS_DATE = T.PROCESS_DATE)");
		sb.append("\n      LEFT JOIN VCUBE.CFLOW CF ON (I.INS_ID = CF.INS_ID AND INTR.INT_END_DATE = CF.CF_PAYMENT_DATE AND CF.PROCESS_DATE = T.PROCESS_DATE)");
		sb.append("\n      LEFT JOIN VCUBE.YIELD_COMPOUNDING YC ON (ID.YLDCMP_ID = YC.YLDCMP_ID)");
		sb.append("\n      LEFT JOIN VCUBE.SIDE SD ON (I.INS_ID = SD.INS_ID AND I.PROCESS_DATE = SD.PROCESS_DATE)");
		sb.append("\n      LEFT JOIN VCUBE.DAY_COUNT_CONV DCC ON (SD.DCC_ID = DCC.DCC_ID)");
		sb.append("\n  WHERE");
		sb.append("\n       INTR.INT_END_DATE > PARAM.PROJ_DATE	),");
		sb.append("\n       ");
		sb.append("\n       ");
		sb.append("\n VP_DAYS AS (  ");
		sb.append("\n  SELECT");
		sb.append("\n      P.PERIOD_DATE");
		sb.append("\n      ,C.*");
		sb.append("\n      ,CASE ");
		sb.append("\n      	WHEN DCC_NAME = '30/360' THEN 360 * (EXTRACT(YEAR FROM INT_END_DATE)-EXTRACT(YEAR FROM P.PERIOD_DATE)) + 30 * (EXTRACT(MONTH FROM INT_END_DATE)-EXTRACT(MONTH FROM P.PERIOD_DATE)) +  (DECODE(EXTRACT(DAY FROM INT_END_DATE),31,30,EXTRACT(DAY FROM INT_END_DATE))-DECODE(EXTRACT(DAY FROM P.PERIOD_DATE),31,30,EXTRACT(DAY FROM P.PERIOD_DATE))) ");
		sb.append("\n      	ELSE INT_END_DATE - P.PERIOD_DATE ");
		sb.append("\n      END ");
		sb.append("\n      	AS DAYS ");
		sb.append("\n      ,CASE ");
		sb.append("\n      	WHEN DCC_NAME = '30/360' THEN 360 * (EXTRACT(YEAR FROM INT_END_DATE)-EXTRACT(YEAR FROM TRAN_SETTLE_DATE)) + 30 * (EXTRACT(MONTH FROM INT_END_DATE)-EXTRACT(MONTH FROM TRAN_SETTLE_DATE)) +  (DECODE(EXTRACT(DAY FROM INT_END_DATE),31,30,EXTRACT(DAY FROM INT_END_DATE))-DECODE(EXTRACT(DAY FROM TRAN_SETTLE_DATE),31,30,EXTRACT(DAY FROM TRAN_SETTLE_DATE)))");
		sb.append("\n      	ELSE INT_PAYMENT_DATE - TRAN_SETTLE_DATE ");
		sb.append("\n      END ");
		sb.append("\n      AS DAYS_FROM_SETTLE");
		sb.append("\n      ,CASE ");
		sb.append("\n      	WHEN DCC_NAME = '30/360' THEN 360 * (EXTRACT(YEAR FROM INT_END_DATE)-EXTRACT(YEAR FROM INT_START_DATE)) + 30 * (EXTRACT(MONTH FROM INT_END_DATE)-EXTRACT(MONTH FROM INT_START_DATE)) +  (DECODE(EXTRACT(DAY FROM INT_END_DATE),31,30,EXTRACT(DAY FROM INT_END_DATE))-DECODE(EXTRACT(DAY FROM INT_START_DATE),31,30,EXTRACT(DAY FROM INT_START_DATE)))");
		sb.append("\n      	ELSE INT_END_DATE - INT_START_DATE ");
		sb.append("\n      END ");
		sb.append("\n      AS COUPON_DAYS ");
		sb.append("\n      ,CASE ");
		sb.append("\n      	WHEN DCC_NAME = '30/360' THEN 360 * (EXTRACT(YEAR FROM PROCESS_DATE)-EXTRACT(YEAR FROM FECHA_INICIO_CUPON)) + 30 * (EXTRACT(MONTH FROM PROCESS_DATE)-EXTRACT(MONTH FROM FECHA_INICIO_CUPON)) +  (DECODE(EXTRACT(DAY FROM PROCESS_DATE),31,30,EXTRACT(DAY FROM PROCESS_DATE))-DECODE(EXTRACT(DAY FROM FECHA_INICIO_CUPON),31,30,EXTRACT(DAY FROM FECHA_INICIO_CUPON)))");
		sb.append("\n      	ELSE PROCESS_DATE - FECHA_INICIO_CUPON ");
		sb.append("\n      END  ");
		sb.append("\n      AS INT_DEV_DAYS ");
		sb.append("\n      ,CASE ");
		sb.append("\n      	WHEN DCC_NAME = '30/360' THEN 360 * (EXTRACT(YEAR FROM INT_END_DATE)-EXTRACT(YEAR FROM F_ULT_COMPRA)) + 30 * (EXTRACT(MONTH FROM INT_END_DATE)-EXTRACT(MONTH FROM F_ULT_COMPRA)) +  (DECODE(EXTRACT(DAY FROM INT_END_DATE),31,30,EXTRACT(DAY FROM INT_END_DATE))-DECODE(EXTRACT(DAY FROM F_ULT_COMPRA),31,30,EXTRACT(DAY FROM F_ULT_COMPRA))) ");
		sb.append("\n      	ELSE INT_END_DATE - F_ULT_COMPRA ");
		sb.append("\n      END ");
		sb.append("\n      	AS DAYS_FECHA_COMPRA");
		sb.append("\n       ,CASE ");
		sb.append("\n      	WHEN DCC_NAME = '30/360' THEN 360 * (EXTRACT(YEAR FROM INT_END_DATE)-EXTRACT(YEAR FROM PROCESS_DATE)) + 30 * (EXTRACT(MONTH FROM INT_END_DATE)-EXTRACT(MONTH FROM PROCESS_DATE)) +  (DECODE(EXTRACT(DAY FROM INT_END_DATE),31,30,EXTRACT(DAY FROM INT_END_DATE))-DECODE(EXTRACT(DAY FROM PROCESS_DATE),31,30,EXTRACT(DAY FROM PROCESS_DATE))) ");
		sb.append("\n      	ELSE INT_END_DATE - PROCESS_DATE ");
		sb.append("\n      END ");
		sb.append("\n      	AS DAYS_PROCESS_DATE");
		sb.append("\n  FROM P_DATES P");
		sb.append("\n          LEFT JOIN CASHFLOWS C ON (C.INT_END_DATE > P.PERIOD_DATE)");
		sb.append("\n  WHERE P.PERIOD_DATE < C.INT_END_DATE	");
		sb.append("\n  ");
		sb.append("\n  ),");
		sb.append("\n ");
		sb.append("\n  ");
		sb.append("\n VP AS (  ");
		sb.append("\n      SELECT");
		sb.append("\n         vp.*");
		sb.append("\n       ,CASE");
		sb.append("\n         WHEN YLDCMP_NAME = 'Semi-Annual (182/360)' THEN 1.0 / POWER ( 1.0 + TRAN_RATE * COUPON_DAYS / 360,  DAYS  / COUPON_DAYS )");
		sb.append("\n         WHEN YLDCMP_NAME = 'Quarterly (91/360)' THEN 1.0 / POWER ( 1.0 + TRAN_RATE * COUPON_DAYS / 360,  DAYS / COUPON_DAYS  )");
		sb.append("\n         WHEN YLDCMP_NAME = 'Monthly' THEN 1.0 / POWER ( 1.0 + TRAN_RATE * COUPON_DAYS / 360,  DAYS / COUPON_DAYS  )        ");
		sb.append("\n         WHEN YLDCMP_NAME = 'None' AND TIPO_VALOR IN ('SP','SC') THEN 1.0 / POWER ( 1.0 + TRAN_RATE * DAYS_FROM_SETTLE / 360, DAYS / DAYS_FROM_SETTLE   )");
		sb.append("\n      ELSE -1000000.0");
		sb.append("\n      END DF");
		sb.append("\n       ,CASE");
		sb.append("\n         WHEN YLDCMP_NAME = 'Semi-Annual (182/360)' THEN 1.0 / POWER ( 1.0 + TRAN_RATE * COUPON_DAYS / 360,  DAYS_FECHA_COMPRA  / COUPON_DAYS )");
		sb.append("\n         WHEN YLDCMP_NAME = 'Quarterly (91/360)' THEN 1.0 / POWER ( 1.0 + TRAN_RATE * COUPON_DAYS / 360,  DAYS_FECHA_COMPRA / COUPON_DAYS  )");
		sb.append("\n         WHEN YLDCMP_NAME = 'Monthly' THEN 1.0 / POWER ( 1.0 + TRAN_RATE * COUPON_DAYS / 360,  DAYS_FECHA_COMPRA / COUPON_DAYS  )        ");
		sb.append("\n         WHEN YLDCMP_NAME = 'None' AND TIPO_VALOR IN ('SP','SC') THEN 1.0 / POWER ( 1.0 + TRAN_RATE * DAYS_FROM_SETTLE / 360, DAYS_FECHA_COMPRA / DAYS_FROM_SETTLE   )");
		sb.append("\n      ELSE -1000000.0");
		sb.append("\n      END DF_FECHA_COMPRA");
		sb.append("\n      ,CASE");
		sb.append("\n         WHEN YLDCMP_NAME = 'Semi-Annual (182/360)' THEN 1.0 / POWER ( 1.0 + TRAN_RATE * COUPON_DAYS / 360,  DAYS_PROCESS_DATE  / COUPON_DAYS )");
		sb.append("\n         WHEN YLDCMP_NAME = 'Quarterly (91/360)' THEN 1.0 / POWER ( 1.0 + TRAN_RATE * COUPON_DAYS / 360,  DAYS_PROCESS_DATE / COUPON_DAYS  )");
		sb.append("\n         WHEN YLDCMP_NAME = 'Monthly' THEN 1.0 / POWER ( 1.0 + TRAN_RATE * COUPON_DAYS / 360,  DAYS_PROCESS_DATE / COUPON_DAYS  )        ");
		sb.append("\n         WHEN YLDCMP_NAME = 'None' AND TIPO_VALOR IN ('SP','SC') THEN 1.0 / POWER ( 1.0 + TRAN_RATE * DAYS_FROM_SETTLE / 360, DAYS_PROCESS_DATE / DAYS_FROM_SETTLE   )");
		sb.append("\n      ELSE -1000000.0");
		sb.append("\n      END DF_PROCESS_DATE");
		sb.append("\n   , CASE WHEN FECHA_FIN_CUPON = INT_END_DATE THEN TASA_CUPON * INT_NOTIONAL * INT_DEV_DAYS / 360.0 ELSE 0.0 END AS INTERES_CPN_YO");
		sb.append("\n  	--, ");
		sb.append("\n  FROM VP_DAYS vp");
		sb.append("\n  ) ");
		sb.append("\n --SACAR LOS VALORES QUE SE NECESITEN");
		sb.append("\n  SELECT  ");
		sb.append("\n 	VP.PROCESS_DATE");
		sb.append("\n 	 ,VP.TRAN_DEAL");
//		sb.append("\n 	 ,VP.PORT_ID");  
		sb.append("\n 	 ,PF.PORT_NAME AS PORT_ID"); //JVG-27022023: UTILIZAR NOMBRE DE PORTAFOLIO
		sb.append("\n 	 ,VP.CCY_NAME");
		sb.append("\n 	 ,CASE WHEN VP.TIPO_VALOR IN ('SC','SP') THEN 'BONO CUPON CERO A DESCUENTO' ELSE 'BONO' END AS INSTRUMENTO");
		sb.append("\n 	 ,VP.TIPO_VALOR");
		sb.append("\n 	 ,VP.EMISORA");
		sb.append("\n 	 ,VP.SERIE");
		sb.append("\n 	 ,VP.T");
		sb.append("\n 	 ,VP.F_ULT_COMPRA");
		sb.append("\n 	 ,VP.F_VTO");
		sb.append("\n 	 ,VP.PLAZO");
		sb.append("\n 	 ,VP.DXV");
		sb.append("\n 	 ,VP.TASA_COMPRA AS TASA_PACT");
		sb.append("\n 	 ,VP.CLAS_CONTABLE AS C");
		sb.append("\n 	 ,VP.TRAN_POSITION AS TITULOS");
		sb.append("\n 	 ,(SUM(TOTAL_AMOUNT* DF_FECHA_COMPRA) * DIVISA + INTERES_CPN)* TRAN_POSITION AS COSTO");
		sb.append("\n 	 ,SUM(TOTAL_AMOUNT* DF) * NVL(RPU.UDI_VALUE,0.0) AS PRECIO");
		sb.append("\n 	 ,VP.TRAN_RATE AS TASA");
		sb.append("\n 	 ,(SUM(TOTAL_AMOUNT* DF_PROCESS_DATE) * DIVISA + INTERES_CPN)* TRAN_POSITION AS VALOR ");
		sb.append("\n 	 ,INTERES_CPN * TRAN_POSITION AS INTERES_CPN");//JVG-27022023: MULTIPLICAR POR LA POSICION
		sb.append("\n 	 ,SUM(TOTAL_AMOUNT* DF) * NVL(RPU.UDI_VALUE,0.0) * TRAN_POSITION AS TOTAL");
		sb.append("\n  	,VP.TASA_CUPON AS TASA_CPN");
		sb.append("\n  	,VP.FECHA_FIN_CUPON AS F_PROX_CUPON");
		sb.append("\n  	,VP.TIT_TECNICAS");
		sb.append("\n  	,VP.TIT_CAPITAL");
		sb.append("\n  	,VP.TIT_OTROS");
		sb.append("\n  	,VP.PERIOD_DATE AS FECHA_CARTERA");
		sb.append("\n  	, 'CARTERA' AS TIPO_CARTERA ");	
		sb.append("\n FROM VP VP ");
		sb.append("\n 	LEFT JOIN PARAMETERS PARAM ON (1=1)");
		sb.append("\n 	LEFT JOIN VCUBE_USER.RISK_PROYECCION_UDI RPU ON (RPU.PERIOD_DATE = VP.PERIOD_DATE AND RPU.PROCESS_DATE = VP.PROCESS_DATE)");
		sb.append("\n 	LEFT JOIN VCUBE.PORTFOLIO PF ON (VP.PORT_ID = PF.PORT_ID)"); //JVG-27022023: UTILIZAR NOMBRE DE PORTAFOLIO
		sb.append("\n WHERE VP.PERIOD_DATE >= PARAM.PROJ_DATE");
		sb.append("\n GROUP BY  ");
		sb.append("\n 	VP.PROCESS_DATE");
		sb.append("\n 	 ,VP.TRAN_DEAL");
//		sb.append("\n 	 ,VP.PORT_ID");
		sb.append("\n 	 ,PF.PORT_NAME");
		sb.append("\n 	 ,VP.CCY_NAME");
		sb.append("\n 	 ,VP.INSTRUMENTO");
		sb.append("\n 	 ,VP.TIPO_VALOR");
		sb.append("\n 	 ,VP.EMISORA");
		sb.append("\n 	 ,VP.SERIE");
		sb.append("\n 	 ,VP.T");
		sb.append("\n 	 ,VP.F_ULT_COMPRA");
		sb.append("\n 	 ,VP.F_VTO");
		sb.append("\n 	 ,VP.PLAZO");
		sb.append("\n 	 ,VP.DXV");
		sb.append("\n 	 ,VP.TASA_COMPRA");
		sb.append("\n 	 ,VP.CLAS_CONTABLE");
		sb.append("\n 	 ,VP.TRAN_POSITION");
		sb.append("\n 	 ,VP.TRAN_RATE");
		sb.append("\n  	,VP.TASA_CUPON");
		sb.append("\n  	,VP.FECHA_FIN_CUPON");
		sb.append("\n  	,VP.FECHA_INICIO_CUPON");
		sb.append("\n  	,VP.TIT_TECNICAS");
		sb.append("\n  	,VP.TIT_CAPITAL");
		sb.append("\n  	,VP.TIT_OTROS");
		sb.append("\n  	,VP.COUPON_DAYS");
		sb.append("\n  	,VP.INT_DEV_DAYS");
		sb.append("\n  	,PS_VALMER");
		sb.append("\n  	,DIVISA");
		sb.append("\n  	,INTERES_CPN");
		sb.append("\n  	,RPU.UDI_VALUE ");
		sb.append("\n  	,VP.PERIOD_DATE");
		sb.append("\n  	ORDER BY VP.TRAN_DEAL, VP.PERIOD_DATE");
		
		
//		VMetrixPopup.createWarning().withLongMessage("SQL\n" + sb.toString()).withOkButton().open();
		
		return sb.toString();
		
	}
	
	private String getReporteCarterasBlotter(){
			//v2.0 JVG-20230105: ESTA FUNCION SOLO PROYECTA LAS COMPRAS DEL BLOTTER
			StringBuilder sb = new StringBuilder();
			
			sb.append("\n /*CTE WITH PARAMETERS FROM JAVA */");
			sb.append("\n WITH PARAMETERS AS (");
			sb.append("\n     SELECT");
			sb.append("\n         TO_DATE('" + dProcessDate.getValue().toString() + "','YYYY-MM-DD') AS PROCESS_DATE");
			sb.append("\n         ,TO_DATE('" + dProcessDate.getValue().toString() + "','YYYY-MM-DD') AS PROJ_DATE");
			sb.append("\n     FROM DUAL	),");
			sb.append("\n     ");
			sb.append("\n     	  P_DATES AS (");
			sb.append("\n SELECT ");
			sb.append("\n 	PERIOD_DATE ");
			sb.append("\n FROM TABLE(VCUBE_USER.PENS.TVF_PROJ_PERIOD(TO_DATE('" + dProcessDate.getValue().toString() + "','YYYY-MM-DD'))) WHERE PERIOD_DATE > TO_DATE('" + dProcessDate.getValue().toString() + "','YYYY-MM-DD')"); //v2.0 JVG-20230105: FILTRO PARA QUE NO UTILIZE LA FECHA DE PROCESO COMO FECHA DE CARTERA
			sb.append("\n fetch first " + k_proy.getValue().toString() + " - 1 rows only");
			sb.append("\n ),");
			sb.append("\n");
			sb.append("\n BLT_OPERACIONES AS (");
			sb.append("\nSELECT ");
			sb.append("\n	ROW_NUMBER() OVER (ORDER BY TRADE_DATE, TRAN_SETTLE_DATE) AS TRAN_DEAL");
			sb.append("\n	, BO.PROCESS_DATE");
			sb.append("\n	, BO.PORTFOLIO");
			sb.append("\n	, C.CCY_NAME");
			sb.append("\n	, ID.INS_ID");
			sb.append("\n	, BO.TICKER ");
			sb.append("\n	, VV.TV AS TIPO_VALOR");
			sb.append("\n 	, VV.EMISORA ");
			sb.append("\n 	, VV.SERIE ");
			sb.append("\n	, 'Trading' AS T");
			sb.append("\n	, BO.TRADE_DATE AS F_ULT_COMPRA");
			sb.append("\n	, BO.TRADE_DATE "); //v2.0 JVG-20230105: PARA OBTENER LA VALORIZACION AL TRADE DATE Y NO A PROCESS DATE
			sb.append("\n	, BO.YIELD/100.00 AS TASA_COMPRA");
			sb.append("\n	, BO.CLASIFICACION_CONTABLE AS CLAS_CONTABLE");
			sb.append("\n	, BO.YIELD/100.00 AS TRAN_RATE");
			sb.append("\n	, VV.PLAZO");
			sb.append("\n	, VV.DIAS_VENC DXV");
			sb.append("\n	, VV.INTERES_DEV AS INTERES_CPN");
			sb.append("\n	, VV.TASA_CUPON_VIGENTE AS TASA_CUPON ");
			sb.append("\n 	, VV.FECHA_FIN_CUPON   ");
			sb.append("\n 	, VV.FECHA_INICIO_CUPON ");
			sb.append("\n 	, BO.CLASIFICACION_RIESGO ");
			sb.append("\n	, BO.STOCK ");
			sb.append("\n	, BO.BUY_SELL ");
			sb.append("\n	, BO.TRAN_SETTLE_DATE ");
			sb.append("\n	, YC.YLDCMP_NAME ");
			sb.append("\n	, DCC.DCC_NAME ");
			sb.append("\n	, nvl(RPU.UDI_VALUE,0.0) AS DIVISA"); //v2.1 JVG-20230215: Modificacion de UDI por UDI Proyectada
			sb.append("\n	, VV.PRECIO_SUCIO");
			sb.append("\n	, BO.PROCESS_DATE + VV.DIAS_VENC AS F_VTO");
			sb.append("\nFROM VCUBE_USER.BLOTTER_OPERACIONES BO");
			sb.append("\n		LEFT JOIN VCUBE.INS_DETAIL ID ON (BO.PROCESS_DATE = ID.PROCESS_DATE AND BO.TICKER = ID.INSD_TICKER)");
			sb.append("\n		LEFT JOIN VCUBE.INSTRUMENT I ON (ID.PROCESS_DATE = I.PROCESS_DATE AND ID.INS_ID  = I.INS_ID)");
			sb.append("\n		LEFT JOIN VCUBE.CURRENCY C ON (I.BASE_CCY_ID = C.CCY_ID)");
			sb.append("\n		LEFT JOIN VCUBE.YIELD_COMPOUNDING YC ON (ID.YLDCMP_ID = YC.YLDCMP_ID)");
			sb.append("\n		LEFT JOIN VCUBE.SIDE SD ON (I.INS_ID = SD.INS_ID AND I.PROCESS_DATE = SD.PROCESS_DATE)");
			sb.append("\n	    LEFT JOIN VCUBE.DAY_COUNT_CONV DCC ON (SD.DCC_ID = DCC.DCC_ID)");
			sb.append("\n	    LEFT JOIN VCUBE_USER.RISK_PROYECCION_UDI RPU ON (RPU.PERIOD_DATE = BO.TRADE_DATE AND RPU.PROCESS_DATE = BO.PROCESS_DATE)"); //v2.1 JVG-20230215: Modificacion de UDI por UDI Proyectada
			sb.append("\n	    LEFT JOIN VCUBE_USER.VECTOR_VALMER vv ON (BO.PROCESS_DATE = VV.PROCESS_DATE AND VV.INSTRUMENTO = BO.TICKER)");
			sb.append("\nWHERE BO.PROCESS_DATE = TO_DATE('" + dProcessDate.getValue().toString() + "','YYYY-MM-DD') AND BO.BUY_SELL = 'Buy'"); //v2.0 JVG-20230105: SE MODIFICA SOLO PARA PROYECCIÓN DE COMPRAS
			sb.append("\nORDER BY TRADE_DATE, TRAN_SETTLE_DATE, TICKER ");
			sb.append("\n),");
			sb.append("\n");
			sb.append("\nCASHFLOWS_BLOTTER AS (");
			sb.append("\nSELECT ");
			sb.append("\n	BO.*");
			sb.append("\n   ,INTR.INT_FLOW");
			sb.append("\n   ,INTR.INT_START_DATE");
			sb.append("\n   ,INTR.INT_END_DATE");
			sb.append("\n   ,INTR.INT_PAYMENT_DATE");
			sb.append("\n   ,INTR.INT_END_DATE - BO.TRADE_DATE AS DAYS "); //v2.0 JVG-20230105: SE USA TRADE DATE
			sb.append("\n   ,INTR.INT_END_DATE - INTR.INT_START_DATE AS COUPON_DAYS");
			sb.append("\n   ,INTR.INT_AMOUNT + NVL(CF.CF_AMOUNT, 0) TOTAL_AMOUNT ");
			sb.append("\n   ,INTR.INT_NOTIONAL ");
			sb.append("\nFROM BLT_OPERACIONES BO");
			sb.append("\n		LEFT JOIN VCUBE.INTEREST INTR ON  (BO.INS_ID = INTR.INS_ID AND INTR.PROCESS_DATE = BO.PROCESS_DATE)");
			sb.append("\n	   	LEFT JOIN VCUBE.CFLOW CF ON (BO.INS_ID = CF.INS_ID AND INTR.INT_PAYMENT_DATE = CF.CF_PAYMENT_DATE AND CF.PROCESS_DATE = BO.PROCESS_DATE)	   	");
			sb.append("\nWHERE INTR.INT_END_DATE > BO.TRADE_DATE"); //v2.0 JVG-20230105: SE USA TRADE DATE
			sb.append("\n),");
			sb.append("\n	     ");
			sb.append("\n");
			sb.append("\nDF_BLOTTER AS (SELECT");
			sb.append("\n	        CS.*");
			sb.append("\n	      ,CASE");
			sb.append("\n	        WHEN YLDCMP_NAME = 'Semi-Annual (182/360)' THEN 1.0 / POWER ( 1.0 + TRAN_RATE * COUPON_DAYS / 360,  DAYS  / COUPON_DAYS )");
			sb.append("\n	        WHEN YLDCMP_NAME = 'Quarterly (91/360)' THEN 1.0 / POWER ( 1.0 + TRAN_RATE * COUPON_DAYS / 360,  DAYS / COUPON_DAYS  )");
			sb.append("\n	        WHEN YLDCMP_NAME = 'Monthly' THEN 1.0 / POWER ( 1.0 + TRAN_RATE * COUPON_DAYS / 360,  DAYS / COUPON_DAYS )");
			sb.append("\n	       WHEN YLDCMP_NAME = 'None' AND SUBSTR(TICKER,1,2) IN ('SP','SC') THEN 1.0 / ( 1.0 + TRAN_RATE * DAYS / 360)");
			sb.append("\n	     ELSE -1000000.0");
			sb.append("\n	     END DF");
			sb.append("\n	 FROM CASHFLOWS_BLOTTER CS),");
			sb.append("\n	 ");
			sb.append("\nBASE_TRANSACTIONS_T1 AS (	 ");
			sb.append("\nSELECT ");
			sb.append("\n	TRAN_DEAL");
			sb.append("\n	,PROCESS_DATE");
			sb.append("\n	,PORTFOLIO");
			sb.append("\n	,CCY_NAME");
			sb.append("\n	,INS_ID");
			sb.append("\n	,TICKER");
			sb.append("\n	,TIPO_VALOR");
			sb.append("\n	,EMISORA");
			sb.append("\n	,SERIE");
			sb.append("\n	,T");
			sb.append("\n	,F_ULT_COMPRA");
			sb.append("\n	,TASA_COMPRA");
			sb.append("\n	,CLAS_CONTABLE");
			sb.append("\n	,TRAN_RATE");
			sb.append("\n	,INTERES_CPN");
			sb.append("\n	,TASA_CUPON");
			sb.append("\n	,FECHA_FIN_CUPON");
			sb.append("\n	,FECHA_INICIO_CUPON");
			sb.append("\n	,CLASIFICACION_RIESGO");
			sb.append("\n	,STOCK");
			sb.append("\n	,BUY_SELL");
			sb.append("\n	,TRAN_SETTLE_DATE");
			sb.append("\n	,YLDCMP_NAME");
			sb.append("\n	,DCC_NAME");
			sb.append("\n	,DIVISA");
			sb.append("\n	,PRECIO_SUCIO");
			sb.append("\n	, PLAZO");
			sb.append("\n	, DXV");
			sb.append("\n	,F_VTO");
			sb.append("\n	,CASE WHEN SUM(TOTAL_AMOUNT*DF*DIVISA) = 0 THEN 0.0 ELSE TRUNC(STOCK/SUM(TOTAL_AMOUNT*DF*DIVISA),0) END AS TRAN_POSITION  "); //v2.1 JVG-20230215: Control de division por 0
			sb.append("\n	,TRADE_DATE");
			sb.append("\nFROM DF_BLOTTER");
			sb.append("\nGROUP BY ");
			sb.append("\n	TRAN_DEAL");
			sb.append("\n	,PROCESS_DATE");
			sb.append("\n	,PORTFOLIO");
			sb.append("\n	,CCY_NAME");
			sb.append("\n	,INS_ID");
			sb.append("\n	,TICKER");
			sb.append("\n	,TIPO_VALOR");
			sb.append("\n	,EMISORA");
			sb.append("\n	,SERIE");
			sb.append("\n	,T");
			sb.append("\n	,F_ULT_COMPRA");
			sb.append("\n	,TASA_COMPRA");
			sb.append("\n	,CLAS_CONTABLE");
			sb.append("\n	,TRAN_RATE");
			sb.append("\n	,INTERES_CPN");
			sb.append("\n	,TASA_CUPON");
			sb.append("\n	,FECHA_FIN_CUPON");
			sb.append("\n	,FECHA_INICIO_CUPON");
			sb.append("\n	,CLASIFICACION_RIESGO");
			sb.append("\n	,STOCK");
			sb.append("\n	,BUY_SELL");
			sb.append("\n	,TRAN_SETTLE_DATE");
			sb.append("\n	,YLDCMP_NAME");
			sb.append("\n	,DCC_NAME");
			sb.append("\n	,DIVISA");
			sb.append("\n	,PRECIO_SUCIO");
			sb.append("\n	, PLAZO");
			sb.append("\n	, DXV");
			sb.append("\n	,F_VTO");
			sb.append("\n	,TRADE_DATE");
			sb.append("\n),");
			sb.append("\n");
			sb.append("\nCASHFLOWS AS (");
			sb.append("\n SELECT");
			sb.append("\n     T.*");
			sb.append("\n     ,CS.INT_FLOW");
			sb.append("\n     ,CS.INT_START_DATE");
			sb.append("\n     ,CS.INT_END_DATE");
			sb.append("\n     ,CS.INT_PAYMENT_DATE");
			sb.append("\n    -- ,LAST_DAY(INTR.INT_END_DATE) P_DATE");
			sb.append("\n     ,CS.TOTAL_AMOUNT");
			sb.append("\n     ,CS.INT_NOTIONAL");
			sb.append("\n FROM BASE_TRANSACTIONS_T1 T");
			sb.append("\n   	 LEFT JOIN CASHFLOWS_BLOTTER CS ON (T.PROCESS_DATE = CS.PROCESS_DATE AND T.TRAN_DEAL = CS.TRAN_DEAL)");
			sb.append("\n WHERE");
			sb.append("\n      CS.INT_END_DATE > T.F_ULT_COMPRA	),");
			sb.append("\n   ");
			sb.append("\n    ");
			sb.append("\n      ");
			sb.append("\nVP_DAYS AS (  ");
			sb.append("\n SELECT");
			sb.append("\n     P.PERIOD_DATE");
			sb.append("\n     ,C.*");
			sb.append("\n     ,CASE ");
			sb.append("\n     	WHEN DCC_NAME = '30/360' THEN 360 * (EXTRACT(YEAR FROM INT_END_DATE)-EXTRACT(YEAR FROM P.PERIOD_DATE)) + 30 * (EXTRACT(MONTH FROM INT_END_DATE)-EXTRACT(MONTH FROM P.PERIOD_DATE)) +  (DECODE(EXTRACT(DAY FROM INT_END_DATE),31,30,EXTRACT(DAY FROM INT_END_DATE))-DECODE(EXTRACT(DAY FROM P.PERIOD_DATE),31,30,EXTRACT(DAY FROM P.PERIOD_DATE))) ");
			sb.append("\n     	ELSE INT_END_DATE - P.PERIOD_DATE ");
			sb.append("\n     END ");
			sb.append("\n     	AS DAYS ");
			sb.append("\n     ,CASE ");
			sb.append("\n     	WHEN DCC_NAME = '30/360' THEN 360 * (EXTRACT(YEAR FROM INT_END_DATE)-EXTRACT(YEAR FROM TRAN_SETTLE_DATE)) + 30 * (EXTRACT(MONTH FROM INT_END_DATE)-EXTRACT(MONTH FROM TRAN_SETTLE_DATE)) +  (DECODE(EXTRACT(DAY FROM INT_END_DATE),31,30,EXTRACT(DAY FROM INT_END_DATE))-DECODE(EXTRACT(DAY FROM TRAN_SETTLE_DATE),31,30,EXTRACT(DAY FROM TRAN_SETTLE_DATE)))");
			sb.append("\n     	ELSE INT_PAYMENT_DATE - TRAN_SETTLE_DATE ");
			sb.append("\n     END ");
			sb.append("\n     AS DAYS_FROM_SETTLE");
			sb.append("\n     ,CASE ");
			sb.append("\n     	WHEN DCC_NAME = '30/360' THEN 360 * (EXTRACT(YEAR FROM INT_END_DATE)-EXTRACT(YEAR FROM INT_START_DATE)) + 30 * (EXTRACT(MONTH FROM INT_END_DATE)-EXTRACT(MONTH FROM INT_START_DATE)) +  (DECODE(EXTRACT(DAY FROM INT_END_DATE),31,30,EXTRACT(DAY FROM INT_END_DATE))-DECODE(EXTRACT(DAY FROM INT_START_DATE),31,30,EXTRACT(DAY FROM INT_START_DATE)))");
			sb.append("\n     	ELSE INT_END_DATE - INT_START_DATE ");
			sb.append("\n     END ");
			sb.append("\n     AS COUPON_DAYS ");
			sb.append("\n     ,CASE ");
			sb.append("\n     	WHEN DCC_NAME = '30/360' THEN 360 * (EXTRACT(YEAR FROM PROCESS_DATE)-EXTRACT(YEAR FROM FECHA_INICIO_CUPON)) + 30 * (EXTRACT(MONTH FROM PROCESS_DATE)-EXTRACT(MONTH FROM FECHA_INICIO_CUPON)) +  (DECODE(EXTRACT(DAY FROM PROCESS_DATE),31,30,EXTRACT(DAY FROM PROCESS_DATE))-DECODE(EXTRACT(DAY FROM FECHA_INICIO_CUPON),31,30,EXTRACT(DAY FROM FECHA_INICIO_CUPON)))");
			sb.append("\n     	ELSE PROCESS_DATE - FECHA_INICIO_CUPON ");
			sb.append("\n     END  ");
			sb.append("\n     AS INT_DEV_DAYS ");
			sb.append("\n     ,CASE ");
			sb.append("\n     	WHEN DCC_NAME = '30/360' THEN 360 * (EXTRACT(YEAR FROM INT_END_DATE)-EXTRACT(YEAR FROM F_ULT_COMPRA)) + 30 * (EXTRACT(MONTH FROM INT_END_DATE)-EXTRACT(MONTH FROM F_ULT_COMPRA)) +  (DECODE(EXTRACT(DAY FROM INT_END_DATE),31,30,EXTRACT(DAY FROM INT_END_DATE))-DECODE(EXTRACT(DAY FROM F_ULT_COMPRA),31,30,EXTRACT(DAY FROM F_ULT_COMPRA))) ");
			sb.append("\n     	ELSE INT_END_DATE - F_ULT_COMPRA ");
			sb.append("\n     END ");
			sb.append("\n     	AS DAYS_FECHA_COMPRA");
			sb.append("\n      ,CASE ");
			sb.append("\n     	WHEN DCC_NAME = '30/360' THEN 360 * (EXTRACT(YEAR FROM INT_END_DATE)-EXTRACT(YEAR FROM PROCESS_DATE)) + 30 * (EXTRACT(MONTH FROM INT_END_DATE)-EXTRACT(MONTH FROM PROCESS_DATE)) +  (DECODE(EXTRACT(DAY FROM INT_END_DATE),31,30,EXTRACT(DAY FROM INT_END_DATE))-DECODE(EXTRACT(DAY FROM PROCESS_DATE),31,30,EXTRACT(DAY FROM PROCESS_DATE))) ");
			sb.append("\n     	ELSE INT_END_DATE - PROCESS_DATE ");
			sb.append("\n     END ");
			sb.append("\n     	AS DAYS_PROCESS_DATE");
			sb.append("\n FROM P_DATES P");
			sb.append("\n         LEFT JOIN CASHFLOWS C ON (C.INT_END_DATE > P.PERIOD_DATE)");
			sb.append("\n WHERE P.PERIOD_DATE < C.INT_END_DATE	");
			sb.append("\n ),");
			sb.append("\n");
			sb.append("\n ");
			sb.append("\nVP AS (  ");
			sb.append("\n     SELECT");
			sb.append("\n        vp.*");
			sb.append("\n      ,CASE");
			sb.append("\n        WHEN YLDCMP_NAME = 'Semi-Annual (182/360)' THEN 1.0 / POWER ( 1.0 + TRAN_RATE * COUPON_DAYS / 360,  DAYS  / COUPON_DAYS )");
			sb.append("\n        WHEN YLDCMP_NAME = 'Quarterly (91/360)' THEN 1.0 / POWER ( 1.0 + TRAN_RATE * COUPON_DAYS / 360,  DAYS / COUPON_DAYS  )");
			sb.append("\n        WHEN YLDCMP_NAME = 'Monthly' THEN 1.0 / POWER ( 1.0 + TRAN_RATE * COUPON_DAYS / 360,  DAYS / COUPON_DAYS  )        ");
			sb.append("\n        WHEN YLDCMP_NAME = 'None' AND TIPO_VALOR IN ('SP','SC') THEN 1.0 / POWER ( 1.0 + TRAN_RATE * DAYS_FROM_SETTLE / 360, DAYS / DAYS_FROM_SETTLE   )");
			sb.append("\n     ELSE -1000000.0");
			sb.append("\n     END DF");
			sb.append("\n      ,CASE");
			sb.append("\n        WHEN YLDCMP_NAME = 'Semi-Annual (182/360)' THEN 1.0 / POWER ( 1.0 + TRAN_RATE * COUPON_DAYS / 360,  DAYS_FECHA_COMPRA  / COUPON_DAYS )");
			sb.append("\n        WHEN YLDCMP_NAME = 'Quarterly (91/360)' THEN 1.0 / POWER ( 1.0 + TRAN_RATE * COUPON_DAYS / 360,  DAYS_FECHA_COMPRA / COUPON_DAYS  )");
			sb.append("\n        WHEN YLDCMP_NAME = 'Monthly' THEN 1.0 / POWER ( 1.0 + TRAN_RATE * COUPON_DAYS / 360,  DAYS_FECHA_COMPRA / COUPON_DAYS  )        ");
			sb.append("\n        WHEN YLDCMP_NAME = 'None' AND TIPO_VALOR IN ('SP','SC') THEN 1.0 / POWER ( 1.0 + TRAN_RATE * DAYS_FROM_SETTLE / 360, DAYS_FECHA_COMPRA / DAYS_FROM_SETTLE   )");
			sb.append("\n     ELSE -1000000.0");
			sb.append("\n     END DF_FECHA_COMPRA");
			sb.append("\n     ,CASE");
			sb.append("\n        WHEN YLDCMP_NAME = 'Semi-Annual (182/360)' THEN 1.0 / POWER ( 1.0 + TRAN_RATE * COUPON_DAYS / 360,  DAYS_PROCESS_DATE  / COUPON_DAYS )");
			sb.append("\n        WHEN YLDCMP_NAME = 'Quarterly (91/360)' THEN 1.0 / POWER ( 1.0 + TRAN_RATE * COUPON_DAYS / 360,  DAYS_PROCESS_DATE / COUPON_DAYS  )");
			sb.append("\n        WHEN YLDCMP_NAME = 'Monthly' THEN 1.0 / POWER ( 1.0 + TRAN_RATE * COUPON_DAYS / 360,  DAYS_PROCESS_DATE / COUPON_DAYS  )        ");
			sb.append("\n        WHEN YLDCMP_NAME = 'None' AND TIPO_VALOR IN ('SP','SC') THEN 1.0 / POWER ( 1.0 + TRAN_RATE * DAYS_FROM_SETTLE / 360, DAYS_PROCESS_DATE / DAYS_FROM_SETTLE   )");
			sb.append("\n     ELSE -1000000.0");
			sb.append("\n     END DF_PROCESS_DATE");
			sb.append("\n  , CASE WHEN FECHA_FIN_CUPON = INT_END_DATE THEN TASA_CUPON * INT_NOTIONAL * INT_DEV_DAYS / 360.0 ELSE 0.0 END AS INTERES_CPN_YO");
			sb.append("\n 	--, ");
			sb.append("\n FROM VP_DAYS vp");
			sb.append("\n ) ");
			sb.append("\n ");
			sb.append("\n--SACAR LOS VALORES QUE SE NECESITEN");
			sb.append("\n SELECT  ");
			sb.append("\n	VP.PROCESS_DATE");
			sb.append("\n	 ,VP.TRAN_DEAL");
//			sb.append("\n	 ,VP.PORTFOLIO AS PORT_ID");
			sb.append("\n 	 ,PF.PORT_NAME AS PORT_ID"); //JVG-27022023: UTILIZAR NOMBRE DE PORTAFOLIO
			sb.append("\n	 ,VP.CCY_NAME");
			sb.append("\n	 ,CASE WHEN VP.TIPO_VALOR IN ('SC','SP') THEN 'BONO CUPON CERO A DESCUENTO' ELSE 'BONO' END AS INSTRUMENTO");
			sb.append("\n	 ,VP.TIPO_VALOR");
			sb.append("\n	 ,VP.EMISORA");
			sb.append("\n	 ,VP.SERIE");
			sb.append("\n	 ,VP.T");
			sb.append("\n	 ,VP.F_ULT_COMPRA");
			sb.append("\n	 ,VP.F_VTO");
			sb.append("\n	 ,VP.PLAZO");
			sb.append("\n	 ,VP.DXV");
			sb.append("\n	 ,VP.TASA_COMPRA AS TASA_PACT");
			sb.append("\n	 ,VP.CLAS_CONTABLE AS C");
			sb.append("\n	 ,VP.TRAN_POSITION AS TITULOS");
			sb.append("\n	 ,(SUM(TOTAL_AMOUNT* DF_FECHA_COMPRA) * DIVISA + INTERES_CPN) * TRAN_POSITION AS COSTO");
			sb.append("\n	 ,SUM(TOTAL_AMOUNT* DF) * nvl(RPU.UDI_VALUE,0.0) AS PRECIO");
			sb.append("\n	 ,VP.TRAN_RATE AS TASA");
			sb.append("\n	 ,(SUM(TOTAL_AMOUNT* DF_PROCESS_DATE) * DIVISA + INTERES_CPN)* TRAN_POSITION AS VALOR ");
			sb.append("\n	 ,INTERES_CPN * TRAN_POSITION AS INTERES_CPN"); //JVG-27022023: MULTIPLICAR POR LA POSICION
			sb.append("\n	 ,SUM(TOTAL_AMOUNT* DF) * nvl(RPU.UDI_VALUE,0.0) * TRAN_POSITION AS TOTAL");
			sb.append("\n 	,VP.TASA_CUPON AS TASA_CPN");
			sb.append("\n 	,VP.FECHA_FIN_CUPON AS F_PROX_CUPON");
			sb.append("\n 	, CASE WHEN VP.CLASIFICACION_RIESGO  = 'Reservas' THEN TRAN_POSITION ELSE 0.0 END AS TIT_TECNICAS");
			sb.append("\n 	, CASE WHEN VP.CLASIFICACION_RIESGO  = 'Capital' THEN TRAN_POSITION ELSE 0.0 END AS TIT_CAPITAL");
			sb.append("\n 	, CASE WHEN VP.CLASIFICACION_RIESGO  = 'Otros Pasivos' THEN TRAN_POSITION ELSE 0.0 END AS TIT_OTROS");
			sb.append("\n 	,VP.PERIOD_DATE AS FECHA_CARTERA");
			sb.append("\n  	, 'BLOTTER OPERACIONES' AS TIPO_CARTERA ");	
			sb.append("\n  	, VP.TRADE_DATE ");
			sb.append("\nFROM VP VP ");
			sb.append("\n	LEFT JOIN PARAMETERS PARAM ON (1=1)");
			sb.append("\n	LEFT JOIN VCUBE_USER.RISK_PROYECCION_UDI RPU ON (RPU.PERIOD_DATE = VP.PERIOD_DATE AND RPU.PROCESS_DATE = VP.PROCESS_DATE)");
			sb.append("\n 	LEFT JOIN VCUBE.PORTFOLIO PF ON (VP.PORTFOLIO = PF.PORT_ID)"); //JVG-27022023: UTILIZAR NOMBRE DE PORTAFOLIO
			sb.append("\nWHERE VP.PERIOD_DATE >= VP.TRADE_DATE");
			sb.append("\nGROUP BY  ");
			sb.append("\n	VP.PROCESS_DATE");
			sb.append("\n	 ,VP.TRAN_DEAL");
//			sb.append("\n	 ,VP.PORTFOLIO");
			sb.append("\n 	 ,PF.PORT_NAME"); //JVG-27022023: UTILIZAR NOMBRE DE PORTAFOLIO
			sb.append("\n	 ,VP.CCY_NAME");
			sb.append("\n	 ,CASE WHEN VP.TIPO_VALOR IN ('SC','SP') THEN 'BONO CUPON CERO A DESCUENTO' ELSE 'BONO' END");
			sb.append("\n	 ,VP.TIPO_VALOR");
			sb.append("\n	 ,VP.EMISORA");
			sb.append("\n	 ,VP.SERIE");
			sb.append("\n	 ,VP.T");
			sb.append("\n	 ,VP.F_ULT_COMPRA");
			sb.append("\n	 ,VP.F_VTO");
			sb.append("\n	 ,VP.PLAZO");
			sb.append("\n	 ,VP.DXV");
			sb.append("\n	 ,VP.TASA_COMPRA");
			sb.append("\n	 ,VP.CLAS_CONTABLE ");
			sb.append("\n	 ,VP.TRAN_POSITION ");
			sb.append("\n	 ,VP.TRAN_RATE");
			sb.append("\n 	,VP.TASA_CUPON ");
			sb.append("\n 	,VP.FECHA_FIN_CUPON ");
			sb.append("\n 	, CASE WHEN VP.CLASIFICACION_RIESGO  = 'Reservas' THEN TRAN_POSITION ELSE 0.0 END ");
			sb.append("\n 	, CASE WHEN VP.CLASIFICACION_RIESGO  = 'Capital' THEN TRAN_POSITION ELSE 0.0 END ");
			sb.append("\n 	, CASE WHEN VP.CLASIFICACION_RIESGO  = 'Otros Pasivos' THEN TRAN_POSITION ELSE 0.0 END");
			sb.append("\n 	,VP.PERIOD_DATE");
			sb.append("\n 	,VP.DIVISA");
			sb.append("\n 	,VP.INTERES_CPN");
			sb.append("\n 	,RPU.UDI_VALUE");
			sb.append("\n 	,VP.TRADE_DATE");
			sb.append("\n	ORDER BY VP.TRAN_DEAL, VP.PERIOD_DATE");
	
	
			//VMetrixPopup.createWarning().withLongMessage("SQL\n" + sb.toString()).withOkButton().open();
			
			return sb.toString();
			
			
		}
		
	private String getReporteCarterasSupEI(){
			//TODO [JIVG]: Se debe hacer de nuevo, bajo las definiciones correctas. 
			StringBuilder sb = new StringBuilder();
			
			sb.append("\n WITH");
			sb.append("\n P_DATES AS (");
			sb.append("\n  SELECT ");
			sb.append("\n  	PERIOD_DATE ");
			sb.append("\n  FROM TABLE(VCUBE_USER.PENS.TVF_PROJ_PERIOD(TO_DATE('" + dProcessDate.getValue().toString() + "','YYYY-MM-DD'))) ");
			sb.append("\n  fetch first " + k_proy.getValue().toString() + " rows only");
			sb.append("\n  ),");
			sb.append("\n SINIESTRALIDAD AS (");
			sb.append("\n SELECT ");
			sb.append("\n 	A.PROCESS_DATE ");
			sb.append("\n 	, A.PROJ_DATE");
			sb.append("\n 	, A.PERIOD_DATE ");
			sb.append("\n 	, A.PORT_ID ");
			sb.append("\n 	, 'SINIESTRALIDAD' AS RINP_NAME ");
			sb.append("\n 	, SUM(A.RPAS_VALUE) VALUE");
			sb.append("\n FROM VCUBE_USER.RISK_PROYECTOR_PASIVOS_PROCESSED A");
			sb.append("\n 	LEFT JOIN vcube_user.RISK_INPUT_DEF RID ON  (A.RINP_ID =RID.RINP_ID)");
			sb.append("\n WHERE ");
			sb.append("\n 	A.PROCESS_DATE =  TO_DATE('" + dProcessDate.getValue().toString() + "','YYYY-MM-DD')");
			sb.append("\n 	AND A.RINP_ID IN (6,7,33)");
			sb.append("\n 	AND A.PERIOD_DATE = ADD_MONTHS(A.PROJ_DATE,1)");
			sb.append("\n GROUP BY ");
			sb.append("\n A.PROCESS_DATE ");
			sb.append("\n , A.PROJ_DATE");
			sb.append("\n , A.PERIOD_DATE ");
			sb.append("\n , A.PORT_ID ");
			sb.append("\n ORDER BY 5,4,2,3),");
			sb.append("\n RESERVAS AS (");
			sb.append("\n SELECT ");
			sb.append("\n 	A.PROCESS_DATE");
			sb.append("\n 	, A.PERIOD_DATE ");
			sb.append("\n 	, A.PROJ_DATE ");
			sb.append("\n 	, A.PORT_ID");
			sb.append("\n 	, RID.RINP_NAME ,");
			sb.append("\n 	SUM(A.RPAS_VALUE*RVAR_VALUE) AS VALUE  ");
			sb.append("\n FROM VCUBE_USER.RISK_PROYECTOR_PASIVOS_PROCESSED A ");
			sb.append("\n 	LEFT JOIN VCUBE_USER.RISK_INPUT_DEF RID ON  (A.RINP_ID =RID.RINP_ID)");
			sb.append("\n 	LEFT JOIN VCUBE_USER.RISK_VARIABLES RV ON (1=1 AND RVAR_NAME = 'RESERVA_CONT_PERCENTAGE')");
			sb.append("\n WHERE ");
			sb.append("\n 	A.PROCESS_DATE = TO_DATE('" + dProcessDate.getValue().toString() + "','YYYY-MM-DD')");
			sb.append("\n 	AND A.RINP_ID IN (1)");
			sb.append("\n 	AND A.PERIOD_DATE <= ADD_MONTHS(A.PROJ_DATE,1)");
			sb.append("\n 	AND A.PERIOD_DATE >= A.PROJ_DATE");
			sb.append("\n GROUP BY ");
			sb.append("\n 	A.PROCESS_DATE");
			sb.append("\n 	, A.PERIOD_DATE ");
			sb.append("\n 	, A.PROJ_DATE ");
			sb.append("\n 	, A.PORT_ID ");
			sb.append("\n 	, RID.RINP_NAME ");
			sb.append("\n ORDER BY 5,4,3,2	),");
			sb.append("\n TASA_TECNICA AS (");
			sb.append("\n SELECT ");
			sb.append("\n 	A.PROCESS_DATE");
			sb.append("\n 	, A.PROJ_DATE");
			sb.append("\n 	, A.PORT_ID ");
			sb.append("\n 	, RID.RINP_NAME");
			sb.append("\n 	, RVEN_VALUE VALUE ");
			sb.append("\n FROM VCUBE_USER.RISK_VENTAS A");
			sb.append("\n 	LEFT JOIN VCUBE_USER.RISK_INPUT_DEF RID ON  (A.RINP_ID =RID.RINP_ID)");
			sb.append("\n WHERE A.RINP_ID = 22");
			sb.append("\n AND A.PROCESS_DATE = TO_DATE('" + dProcessDate.getValue().toString() + "','YYYY-MM-DD')");
			sb.append("\n ORDER BY 4,3,2	),");
			sb.append("\n CONT_FOND_ESP AS (");
			sb.append("\n SELECT");
			sb.append("\n 	A.PROCESS_DATE");
			sb.append("\n 	, ADD_MONTHS(A.PROJ_DATE,1) PROJ_DATE");
			sb.append("\n 	, A.PORT_ID");
			sb.append("\n 	, SUM(CASE WHEN A.PROJ_DATE = A.PERIOD_DATE THEN A.VALUE * POWER(1+E.VALUE,1/12.0) ELSE -A.VALUE END) CONT_FOND_ESP");
			sb.append("\n FROM RESERVAS A ");
			sb.append("\n 	LEFT JOIN TASA_TECNICA E ON (A.PROCESS_DATE = E.PROCESS_DATE AND A.PROJ_DATE = E.PROJ_DATE AND A.PORT_ID = E.PORT_ID)");
			sb.append("\n GROUP BY ");
			sb.append("\n 	A.PROCESS_DATE");
			sb.append("\n 	, ADD_MONTHS(A.PROJ_DATE,1)");
			sb.append("\n 	, A.PORT_ID");
			sb.append("\n ORDER BY 3,2	),");
			sb.append("\n WACC AS (");
			sb.append("\n SELECT ");
			sb.append("\n 	POWER(1+RPV.RPAR_VALUE_DOUBLE,1/12.0)-1 WACC_MENSUAL ");
			sb.append("\n FROM VCUBE_USER.RISK_PARAM_VALUE RPV ");
			sb.append("\n 	LEFT JOIN VCUBE_USER.RISK_PARAM_DEF RPD ON (RPV.RPAR_ID=RPD.RPAR_ID)");
			sb.append("\n WHERE ");
			sb.append("\n RPV_DATE = TO_DATE('" + dProcessDate.getValue().toString() + "','YYYY-MM-DD') ");
			sb.append("\n AND RPD.RPAR_NAME = 'WACC'	),");
			sb.append("\n COSTO_REQ AS (");
			sb.append("\n SELECT ");
			sb.append("\n 	A.PROCESS_DATE");
			sb.append("\n 	, ADD_MONTHS(A.PROJ_DATE,1) PROJ_DATE ");
			sb.append("\n 	, A.PORT_ID");
			sb.append("\n 	, SUM(A.RPAS_VALUE*ROUND(WACC.WACC_MENSUAL,6)) COSTO_REQ  ");
			sb.append("\n FROM VCUBE_USER.RISK_PROYECTOR_PASIVOS_PROCESSED A ");
			sb.append("\n 	LEFT JOIN VCUBE_USER.RISK_INPUT_DEF RID ON  (A.RINP_ID =RID.RINP_ID)");
			sb.append("\n 	LEFT JOIN WACC WACC ON (1=1)");
			sb.append("\n WHERE ");
			sb.append("\n 	A.PROCESS_DATE = TO_DATE('" + dProcessDate.getValue().toString() + "','YYYY-MM-DD')");
			sb.append("\n 	AND A.RINP_ID IN (9)");
			sb.append("\n 	AND A.PERIOD_DATE = A.PROJ_DATE");
			sb.append("\n GROUP BY ");
			sb.append("\n A.PROCESS_DATE");
			sb.append("\n , ADD_MONTHS(A.PROJ_DATE,1) ");
			sb.append("\n , A.PORT_ID	),");
			sb.append("\n GASTOS AS (");
			sb.append("\n SELECT ");
			sb.append("\n 	A.PROCESS_DATE ");
			sb.append("\n 	, A.PERIOD_DATE ");
			sb.append("\n 	, PORT_ID ");
			sb.append("\n 	,  RID.RINP_NAME ");
			sb.append("\n 	, SUM(A.RGAS_VALUE) GASTOS  ");
			sb.append("\n FROM VCUBE_USER.RISK_GASTOS A");
			sb.append("\n 	LEFT JOIN VCUBE_USER.RISK_INPUT_DEF RID ON  (A.RINP_ID =RID.RINP_ID)");
			sb.append("\n WHERE ");
			sb.append("\n 	A.RINP_ID IN (11) ");
			sb.append("\n 	AND PROCESS_DATE = TO_DATE('" + dProcessDate.getValue().toString() + "','YYYY-MM-DD')");
			sb.append("\n GROUP BY ");
			sb.append("\n 	A.PROCESS_DATE ");
			sb.append("\n 	, A.PERIOD_DATE  ");
			sb.append("\n 	, PORT_ID ");
			sb.append("\n 	,  RID.RINP_NAME");
			sb.append("\n ORDER BY 4,3,2	),");
			sb.append("\n EGRESOS AS (");
			sb.append("\n SELECT ");
			sb.append("\n 	A.PROCESS_DATE");
			sb.append("\n 	, A.PERIOD_DATE");
			sb.append("\n 	, A.PORT_ID");
			sb.append("\n 	, A.VALUE AS SINIESTRALIDAD,B.CONT_FOND_ESP , C.COSTO_REQ , D.GASTOS ");
			sb.append("\n 	, A.VALUE + B.CONT_FOND_ESP + C.COSTO_REQ + D.GASTOS AS EGRESOS");
			sb.append("\n FROM SINIESTRALIDAD A");
			sb.append("\n 	LEFT JOIN CONT_FOND_ESP B ON (A.PERIOD_DATE = B.PROJ_DATE AND A.PORT_ID = B.PORT_ID)");
			sb.append("\n 	LEFT JOIN COSTO_REQ C ON (A.PERIOD_DATE = C.PROJ_DATE AND A.PORT_ID = C.PORT_ID)");
			sb.append("\n 	LEFT JOIN GASTOS D ON (A.PERIOD_DATE = D.PERIOD_DATE AND A.PORT_ID = D.PORT_ID)	),");
			sb.append("\n   PROY_UDI AS (");
			sb.append("\n SELECT ");
			sb.append("\n 	RPU.PERIOD_DATE PROJ_DATE");
			sb.append("\n 	, RPU.UDI_VALUE");
			sb.append("\n FROM VCUBE_USER.RISK_PROYECCION_UDI RPU");
			sb.append("\n WHERE ");
			sb.append("\n 	RPU.PROCESS_DATE = TO_DATE('" + dProcessDate.getValue().toString() + "','YYYY-MM-DD')");
			sb.append("\n ORDER BY 1	),");
			sb.append("\n  FLUJOS AS (");
			sb.append("\n SELECT ");
			sb.append("\n A.PROCESS_DATE");
			sb.append("\n , A.PERIOD_DATE");
			sb.append("\n , A.PORT_ID ");
			sb.append("\n , SUM(A.MT_VALUE_MO * B.UDI_VALUE)  AS FLUJOS");
			sb.append("\n FROM VCUBE_USER.RISK_PROYECTOR_ACTIVOS A ");
			sb.append("\n 	LEFT JOIN PROY_UDI B ON A.PROJ_DATE = B.PROJ_DATE");
			sb.append("\n WHERE ");
			sb.append("\n 	A.PROCESS_DATE = TO_DATE('" + dProcessDate.getValue().toString() + "','YYYY-MM-DD')");
			sb.append("\n 	AND A.MT_NAME = 'FLUJOS'");
			sb.append("\n GROUP BY ");
			sb.append("\n 	A.PROCESS_DATE");
			sb.append("\n 	, A.PERIOD_DATE ");
			sb.append("\n 	, A.PORT_ID");
			sb.append("\n ORDER BY 1	),");
			sb.append("\n ");
			sb.append("\n OPERACIONES AS (");
			sb.append("\n SELECT ");
			sb.append("\n 	A.PROCESS_DATE ");
			sb.append("\n 	,A.PORT_ID");
			sb.append("\n 	,A.PERIOD_DATE AS TRADE_DATE ");
			sb.append("\n 	, VCUBE_USER.PENS.SVF_FIRST_UDIBONO(A.PROCESS_DATE, A.PERIOD_DATE) AS TICKER");
			sb.append("\n 	, SUM(TRUNC((C.FLUJOS - A.EGRESOS)/B.PRECIO_SUCIO,0)) POS ");
			sb.append("\n 	, B.RENDIMIENTO AS YIELD");
			sb.append("\n FROM EGRESOS A ");
			sb.append("\n 	LEFT JOIN VCUBE_USER.VECTOR_VALMER B ON (VCUBE_USER.PENS.SVF_FIRST_UDIBONO(A.PROCESS_DATE, A.PERIOD_DATE) =  B.INSTRUMENTO AND A.PROCESS_DATE = B.PROCESS_DATE) ");
			sb.append("\n 	LEFT JOIN FLUJOS C ON (A.PERIOD_DATE = C.PERIOD_DATE AND A.PORT_ID = C.PORT_ID)");
			sb.append("\n GROUP BY A.PROCESS_DATE ");
			sb.append("\n ,A.PORT_ID");
			sb.append("\n ,A.PERIOD_DATE ");
			sb.append("\n ,VCUBE_USER.PENS.SVF_FIRST_UDIBONO(A.PROCESS_DATE, A.PERIOD_DATE)");
			sb.append("\n , B.RENDIMIENTO");
			sb.append("\n 	ORDER BY 3,2),");
			sb.append("\n 	");
			sb.append("\n BASE_TRANSACTIONS_T1 AS (	");
			sb.append("\n SELECT  ");
			sb.append("\n ROW_NUMBER() OVER (ORDER BY TRADE_DATE) AS TRAN_DEAL");
			sb.append("\n , OPE.PROCESS_DATE");
			sb.append("\n , OPE.PORT_ID");
			sb.append("\n , OPE.POS AS TRAN_POSITION");
			sb.append("\n , C.CCY_NAME");
			sb.append("\n , ID.INS_ID");
			sb.append("\n , OPE.TICKER ");
			sb.append("\n , VV.TV AS TIPO_VALOR");
			sb.append("\n , VV.EMISORA ");
			sb.append("\n , VV.SERIE ");
			sb.append("\n , 'Trading' AS T");
			sb.append("\n , OPE.TRADE_DATE AS F_ULT_COMPRA");
			sb.append("\n , OPE.YIELD/100.00 AS TASA_COMPRA");
			sb.append("\n , 'A Vencimiento' AS CLAS_CONTABLE");
			sb.append("\n , OPE.YIELD/100.00 AS TRAN_RATE");
			sb.append("\n , VV.PLAZO");
			sb.append("\n , VV.DIAS_VENC DXV");
			sb.append("\n , VV.INTERES_DEV AS INTERES_CPN");
			sb.append("\n , VV.TASA_CUPON_VIGENTE AS TASA_CUPON ");
			sb.append("\n , VV.FECHA_FIN_CUPON   ");
			sb.append("\n , VV.FECHA_INICIO_CUPON ");
			sb.append("\n , 'Reservas' AS CLASIFICACION_RIESGO   ");
			sb.append("\n , OPE.TRADE_DATE AS TRAN_SETTLE_DATE ");
			sb.append("\n , YC.YLDCMP_NAME ");
			sb.append("\n , DCC.DCC_NAME ");
			sb.append("\n , MON.PRECIO_SUCIO AS DIVISA");
			sb.append("\n , VV.PRECIO_SUCIO");
			sb.append("\n , OPE.PROCESS_DATE + VV.DIAS_VENC AS F_VTO");
			sb.append("\n FROM OPERACIONES OPE");
			sb.append("\n 	LEFT JOIN VCUBE.INS_DETAIL ID ON (OPE.PROCESS_DATE = ID.PROCESS_DATE AND OPE.TICKER = ID.INSD_TICKER)");
			sb.append("\n 	LEFT JOIN VCUBE.INSTRUMENT I ON (ID.PROCESS_DATE = I.PROCESS_DATE AND ID.INS_ID  = I.INS_ID)");
			sb.append("\n 	LEFT JOIN VCUBE.CURRENCY C ON (I.BASE_CCY_ID = C.CCY_ID)");
			sb.append("\n 	LEFT JOIN VCUBE.YIELD_COMPOUNDING YC ON (ID.YLDCMP_ID = YC.YLDCMP_ID)");
			sb.append("\n 	LEFT JOIN VCUBE.SIDE SD ON (I.INS_ID = SD.INS_ID AND I.PROCESS_DATE = SD.PROCESS_DATE)");
			sb.append("\n     LEFT JOIN VCUBE.DAY_COUNT_CONV DCC ON (SD.DCC_ID = DCC.DCC_ID)");
			sb.append("\n     LEFT JOIN VCUBE_USER.VECTOR_VALMER MON ON (OPE.PROCESS_DATE = MON.PROCESS_DATE AND MON.INSTRUMENTO = concat(concat(concat('*C_MXP', C.CCY_NAME), '_'), C.CCY_NAME))");
			sb.append("\n     LEFT JOIN VCUBE_USER.VECTOR_VALMER vv ON (OPE.PROCESS_DATE = VV.PROCESS_DATE AND VV.INSTRUMENTO = OPE.TICKER)");
			sb.append("\n ),");
			sb.append("\n ");
			sb.append("\n CASHFLOWS AS (");
			sb.append("\n SELECT ");
			sb.append("\n 	BO.*");
			sb.append("\n    ,INTR.INT_FLOW");
			sb.append("\n    ,INTR.INT_START_DATE");
			sb.append("\n    ,INTR.INT_END_DATE");
			sb.append("\n    ,INTR.INT_PAYMENT_DATE");
			sb.append("\n    ,INTR.INT_AMOUNT + NVL(CF.CF_AMOUNT, 0) TOTAL_AMOUNT ");
			sb.append("\n    ,INTR.INT_NOTIONAL ");
			sb.append("\n FROM BASE_TRANSACTIONS_T1 BO");
			sb.append("\n 		LEFT JOIN VCUBE.INTEREST INTR ON  (BO.INS_ID = INTR.INS_ID AND INTR.PROCESS_DATE = BO.PROCESS_DATE)");
			sb.append("\n 	   	LEFT JOIN VCUBE.CFLOW CF ON (BO.INS_ID = CF.INS_ID AND INTR.INT_PAYMENT_DATE = CF.CF_PAYMENT_DATE AND CF.PROCESS_DATE = BO.PROCESS_DATE)	   	");
			sb.append("\n WHERE INTR.INT_END_DATE > BO.PROCESS_DATE /*SE PUEDE USAR EL BO.TRADE_DATE EN CASO DE QUE SE REQUIERA EL CALCULO POR ESTA FECHA*/");
			sb.append("\n ),");
			sb.append("\n         ");
			sb.append("\n VP_DAYS AS (  ");
			sb.append("\n  SELECT");
			sb.append("\n      P.PERIOD_DATE");
			sb.append("\n      ,C.*");
			sb.append("\n      ,CASE ");
			sb.append("\n      	WHEN DCC_NAME = '30/360' THEN 360 * (EXTRACT(YEAR FROM INT_END_DATE)-EXTRACT(YEAR FROM P.PERIOD_DATE)) + 30 * (EXTRACT(MONTH FROM INT_END_DATE)-EXTRACT(MONTH FROM P.PERIOD_DATE)) +  (DECODE(EXTRACT(DAY FROM INT_END_DATE),31,30,EXTRACT(DAY FROM INT_END_DATE))-DECODE(EXTRACT(DAY FROM P.PERIOD_DATE),31,30,EXTRACT(DAY FROM P.PERIOD_DATE))) ");
			sb.append("\n      	ELSE INT_END_DATE - P.PERIOD_DATE ");
			sb.append("\n      END ");
			sb.append("\n      	AS DAYS ");
			sb.append("\n      ,CASE ");
			sb.append("\n      	WHEN DCC_NAME = '30/360' THEN 360 * (EXTRACT(YEAR FROM INT_END_DATE)-EXTRACT(YEAR FROM TRAN_SETTLE_DATE)) + 30 * (EXTRACT(MONTH FROM INT_END_DATE)-EXTRACT(MONTH FROM TRAN_SETTLE_DATE)) +  (DECODE(EXTRACT(DAY FROM INT_END_DATE),31,30,EXTRACT(DAY FROM INT_END_DATE))-DECODE(EXTRACT(DAY FROM TRAN_SETTLE_DATE),31,30,EXTRACT(DAY FROM TRAN_SETTLE_DATE)))");
			sb.append("\n      	ELSE INT_PAYMENT_DATE - TRAN_SETTLE_DATE ");
			sb.append("\n      END ");
			sb.append("\n      AS DAYS_FROM_SETTLE");
			sb.append("\n      ,CASE ");
			sb.append("\n      	WHEN DCC_NAME = '30/360' THEN 360 * (EXTRACT(YEAR FROM INT_END_DATE)-EXTRACT(YEAR FROM INT_START_DATE)) + 30 * (EXTRACT(MONTH FROM INT_END_DATE)-EXTRACT(MONTH FROM INT_START_DATE)) +  (DECODE(EXTRACT(DAY FROM INT_END_DATE),31,30,EXTRACT(DAY FROM INT_END_DATE))-DECODE(EXTRACT(DAY FROM INT_START_DATE),31,30,EXTRACT(DAY FROM INT_START_DATE)))");
			sb.append("\n      	ELSE INT_END_DATE - INT_START_DATE ");
			sb.append("\n      END ");
			sb.append("\n      AS COUPON_DAYS ");
			sb.append("\n      ,CASE ");
			sb.append("\n      	WHEN DCC_NAME = '30/360' THEN 360 * (EXTRACT(YEAR FROM PROCESS_DATE)-EXTRACT(YEAR FROM FECHA_INICIO_CUPON)) + 30 * (EXTRACT(MONTH FROM PROCESS_DATE)-EXTRACT(MONTH FROM FECHA_INICIO_CUPON)) +  (DECODE(EXTRACT(DAY FROM PROCESS_DATE),31,30,EXTRACT(DAY FROM PROCESS_DATE))-DECODE(EXTRACT(DAY FROM FECHA_INICIO_CUPON),31,30,EXTRACT(DAY FROM FECHA_INICIO_CUPON)))");
			sb.append("\n      	ELSE PROCESS_DATE - FECHA_INICIO_CUPON ");
			sb.append("\n      END  ");
			sb.append("\n      AS INT_DEV_DAYS ");
			sb.append("\n      ,CASE ");
			sb.append("\n      	WHEN DCC_NAME = '30/360' THEN 360 * (EXTRACT(YEAR FROM INT_END_DATE)-EXTRACT(YEAR FROM F_ULT_COMPRA)) + 30 * (EXTRACT(MONTH FROM INT_END_DATE)-EXTRACT(MONTH FROM F_ULT_COMPRA)) +  (DECODE(EXTRACT(DAY FROM INT_END_DATE),31,30,EXTRACT(DAY FROM INT_END_DATE))-DECODE(EXTRACT(DAY FROM F_ULT_COMPRA),31,30,EXTRACT(DAY FROM F_ULT_COMPRA))) ");
			sb.append("\n      	ELSE INT_END_DATE - F_ULT_COMPRA ");
			sb.append("\n      END ");
			sb.append("\n      	AS DAYS_FECHA_COMPRA");
			sb.append("\n       ,CASE ");
			sb.append("\n      	WHEN DCC_NAME = '30/360' THEN 360 * (EXTRACT(YEAR FROM INT_END_DATE)-EXTRACT(YEAR FROM PROCESS_DATE)) + 30 * (EXTRACT(MONTH FROM INT_END_DATE)-EXTRACT(MONTH FROM PROCESS_DATE)) +  (DECODE(EXTRACT(DAY FROM INT_END_DATE),31,30,EXTRACT(DAY FROM INT_END_DATE))-DECODE(EXTRACT(DAY FROM PROCESS_DATE),31,30,EXTRACT(DAY FROM PROCESS_DATE))) ");
			sb.append("\n      	ELSE INT_END_DATE - PROCESS_DATE ");
			sb.append("\n      END ");
			sb.append("\n      	AS DAYS_PROCESS_DATE");
			sb.append("\n  FROM P_DATES P");
			sb.append("\n          LEFT JOIN CASHFLOWS C ON (C.INT_END_DATE > P.PERIOD_DATE)");
			sb.append("\n  WHERE P.PERIOD_DATE < C.INT_END_DATE	");
			sb.append("\n  ),");
			sb.append("\n ");
			sb.append("\n  ");
			sb.append("\n VP AS (  ");
			sb.append("\n      SELECT");
			sb.append("\n         vp.*");
			sb.append("\n       ,CASE");
			sb.append("\n         WHEN YLDCMP_NAME = 'Semi-Annual (182/360)' THEN 1.0 / POWER ( 1.0 + TRAN_RATE * COUPON_DAYS / 360,  DAYS  / COUPON_DAYS )");
			sb.append("\n         WHEN YLDCMP_NAME = 'Quarterly (91/360)' THEN 1.0 / POWER ( 1.0 + TRAN_RATE * COUPON_DAYS / 360,  DAYS / COUPON_DAYS  )");
			sb.append("\n         WHEN YLDCMP_NAME = 'Monthly' THEN 1.0 / POWER ( 1.0 + TRAN_RATE * COUPON_DAYS / 360,  DAYS / COUPON_DAYS  )        ");
			sb.append("\n         WHEN YLDCMP_NAME = 'None' AND TIPO_VALOR IN ('SP','SC') THEN 1.0 / POWER ( 1.0 + TRAN_RATE * DAYS_FROM_SETTLE / 360, DAYS / DAYS_FROM_SETTLE   )");
			sb.append("\n      ELSE -1000000.0");
			sb.append("\n      END DF");
			sb.append("\n       ,CASE");
			sb.append("\n         WHEN YLDCMP_NAME = 'Semi-Annual (182/360)' THEN 1.0 / POWER ( 1.0 + TRAN_RATE * COUPON_DAYS / 360,  DAYS_FECHA_COMPRA  / COUPON_DAYS )");
			sb.append("\n         WHEN YLDCMP_NAME = 'Quarterly (91/360)' THEN 1.0 / POWER ( 1.0 + TRAN_RATE * COUPON_DAYS / 360,  DAYS_FECHA_COMPRA / COUPON_DAYS  )");
			sb.append("\n         WHEN YLDCMP_NAME = 'Monthly' THEN 1.0 / POWER ( 1.0 + TRAN_RATE * COUPON_DAYS / 360,  DAYS_FECHA_COMPRA / COUPON_DAYS  )        ");
			sb.append("\n         WHEN YLDCMP_NAME = 'None' AND TIPO_VALOR IN ('SP','SC') THEN 1.0 / POWER ( 1.0 + TRAN_RATE * DAYS_FROM_SETTLE / 360, DAYS_FECHA_COMPRA / DAYS_FROM_SETTLE   )");
			sb.append("\n      ELSE -1000000.0");
			sb.append("\n      END DF_FECHA_COMPRA");
			sb.append("\n      ,CASE");
			sb.append("\n         WHEN YLDCMP_NAME = 'Semi-Annual (182/360)' THEN 1.0 / POWER ( 1.0 + TRAN_RATE * COUPON_DAYS / 360,  DAYS_PROCESS_DATE  / COUPON_DAYS )");
			sb.append("\n         WHEN YLDCMP_NAME = 'Quarterly (91/360)' THEN 1.0 / POWER ( 1.0 + TRAN_RATE * COUPON_DAYS / 360,  DAYS_PROCESS_DATE / COUPON_DAYS  )");
			sb.append("\n         WHEN YLDCMP_NAME = 'Monthly' THEN 1.0 / POWER ( 1.0 + TRAN_RATE * COUPON_DAYS / 360,  DAYS_PROCESS_DATE / COUPON_DAYS  )        ");
			sb.append("\n         WHEN YLDCMP_NAME = 'None' AND TIPO_VALOR IN ('SP','SC') THEN 1.0 / POWER ( 1.0 + TRAN_RATE * DAYS_FROM_SETTLE / 360, DAYS_PROCESS_DATE / DAYS_FROM_SETTLE   )");
			sb.append("\n      ELSE -1000000.0");
			sb.append("\n      END DF_PROCESS_DATE");
			sb.append("\n   , CASE WHEN FECHA_FIN_CUPON = INT_END_DATE THEN TASA_CUPON * INT_NOTIONAL * INT_DEV_DAYS / 360.0 ELSE 0.0 END AS INTERES_CPN_YO");
			sb.append("\n  FROM VP_DAYS vp");
			sb.append("\n  )");
			sb.append("\n  ");
			sb.append("\n  SELECT  ");
			sb.append("\n 	VP.PROCESS_DATE");
			sb.append("\n 	 ,VP.TRAN_DEAL");
			sb.append("\n 	 ,VP.PORT_ID");
			sb.append("\n 	 ,VP.CCY_NAME");
			sb.append("\n 	 ,CASE WHEN VP.TIPO_VALOR IN ('SC','SP') THEN 'BONO CUPON CERO A DESCUENTO' ELSE 'BONO' END AS INSTRUMENTO");
			sb.append("\n 	 ,VP.TIPO_VALOR");
			sb.append("\n 	 ,VP.EMISORA");
			sb.append("\n 	 ,VP.SERIE");
			sb.append("\n 	 ,VP.T");
			sb.append("\n 	 ,VP.F_ULT_COMPRA");
			sb.append("\n 	 ,VP.F_VTO");
			sb.append("\n 	 ,VP.PLAZO");
			sb.append("\n 	 ,VP.DXV");
			sb.append("\n 	 ,VP.TASA_COMPRA  AS TASA_PACT");
			sb.append("\n 	 ,VP.CLAS_CONTABLE AS C");
			sb.append("\n 	 ,VP.TRAN_POSITION AS TITULOS");
			sb.append("\n 	 ,(SUM(TOTAL_AMOUNT* DF_FECHA_COMPRA) * DIVISA + INTERES_CPN) * TRAN_POSITION AS COSTO");
			sb.append("\n 	 ,SUM(TOTAL_AMOUNT* DF) * nvl(RPU.UDI_VALUE,0.0) AS PRECIO");
			sb.append("\n 	 ,VP.TRAN_RATE AS TASA");
			sb.append("\n 	 ,(SUM(TOTAL_AMOUNT* DF_PROCESS_DATE) * DIVISA + INTERES_CPN)* TRAN_POSITION AS VALOR ");
			sb.append("\n 	 ,INTERES_CPN * TRAN_POSITION AS INTERES_CPN"); //JVG-27022023: MULTIPLICAR POR LA POSICION
			sb.append("\n 	 ,SUM(TOTAL_AMOUNT* DF) * nvl(RPU.UDI_VALUE,0.0) * TRAN_POSITION AS TOTAL");
			sb.append("\n  	,VP.TASA_CUPON AS TASA_CPN");
			sb.append("\n  	,VP.FECHA_FIN_CUPON AS F_PROX_CUPON");
			sb.append("\n  	, CASE WHEN VP.CLASIFICACION_RIESGO  = 'Reservas' THEN TRAN_POSITION ELSE 0.0 END AS TIT_TECNICAS");
			sb.append("\n  	, CASE WHEN VP.CLASIFICACION_RIESGO  = 'Capital' THEN TRAN_POSITION ELSE 0.0 END AS TIT_CAPITAL");
			sb.append("\n  	, CASE WHEN VP.CLASIFICACION_RIESGO  = 'Otros Pasivos' THEN TRAN_POSITION ELSE 0.0 END AS TIT_OTROS");
			sb.append("\n  	,VP.PERIOD_DATE AS FECHA_CARTERA");
			sb.append("\n  	, 'SUPUESTOS EGRESOS/INGRESOS' AS TIPO_CARTERA");
			sb.append("\n FROM VP VP ");
			sb.append("\n 	LEFT JOIN VCUBE_USER.RISK_PROYECCION_UDI RPU ON (RPU.PERIOD_DATE = VP.PERIOD_DATE AND RPU.PROCESS_DATE = VP.PROCESS_DATE)");
			sb.append("\n WHERE VP.PERIOD_DATE >= VP.PROCESS_DATE");
			sb.append("\n GROUP BY  ");
			sb.append("\n 	VP.PROCESS_DATE");
			sb.append("\n 	 ,VP.TRAN_DEAL");
			sb.append("\n 	 ,VP.PORT_ID");
			sb.append("\n 	 ,VP.CCY_NAME");
			sb.append("\n 	 ,CASE WHEN VP.TIPO_VALOR IN ('SC','SP') THEN 'BONO CUPON CERO A DESCUENTO' ELSE 'BONO' END");
			sb.append("\n 	 ,VP.TIPO_VALOR");
			sb.append("\n 	 ,VP.EMISORA");
			sb.append("\n 	 ,VP.SERIE");
			sb.append("\n 	 ,VP.T");
			sb.append("\n 	 ,VP.F_ULT_COMPRA");
			sb.append("\n 	 ,VP.F_VTO");
			sb.append("\n 	 ,VP.PLAZO");
			sb.append("\n 	 ,VP.DXV");
			sb.append("\n 	 ,VP.TASA_COMPRA");
			sb.append("\n 	 ,VP.CLAS_CONTABLE ");
			sb.append("\n 	 ,VP.TRAN_POSITION ");
			sb.append("\n 	 ,VP.TRAN_RATE");
			sb.append("\n  	,VP.TASA_CUPON ");
			sb.append("\n  	,VP.FECHA_FIN_CUPON ");
			sb.append("\n  	, CASE WHEN VP.CLASIFICACION_RIESGO  = 'Reservas' THEN TRAN_POSITION ELSE 0.0 END ");
			sb.append("\n  	, CASE WHEN VP.CLASIFICACION_RIESGO  = 'Capital' THEN TRAN_POSITION ELSE 0.0 END ");
			sb.append("\n  	, CASE WHEN VP.CLASIFICACION_RIESGO  = 'Otros Pasivos' THEN TRAN_POSITION ELSE 0.0 END");
			sb.append("\n  	,VP.PERIOD_DATE");
			sb.append("\n  	,VP.DIVISA");
			sb.append("\n  	,VP.INTERES_CPN");
			sb.append("\n  	,RPU.UDI_VALUE");
			sb.append("\n 	ORDER BY VP.TRAN_DEAL, VP.PERIOD_DATE");
		
			return sb.toString();
			
	}
		
	private String getBlotterVentas(){
		//v2.0 JVG-20230105: Consulta para obtener resultados de blotter solo por ventas
		StringBuilder sb = new StringBuilder();
		
		sb.append("\n WITH  CLASIFICACIONES AS(");
		sb.append("\n         SELECT");
		sb.append("\n A.PROCESS_DATE");
		sb.append("\n         ,A.TRAN_ID");
		sb.append("\n         ,A.UTD_VALUE CLAS_CONTABLE");
		sb.append("\n         ,B.UTD_VALUE CLAS_RIESGO");
		sb.append("\n FROM VCUBE.USER_TRAN_DEF_STRING A");
		sb.append("\n LEFT JOIN VCUBE.USER_TRAN_DEF_STRING B ON (A.TRAN_ID = B.TRAN_ID AND A.PROCESS_DATE = B.PROCESS_DATE  AND B.UTD_ID = "+EnumsPensionesUserTranDef.UTD_PENS_TRAN_CLASIF_RIESGO.toInt()+")");
		sb.append("\n WHERE A.UTD_ID = "+EnumsPensionesUserTranDef.UTD_PENS_TRAN_CLASIF_CONTABLE.toInt()+"	AND A.PROCESS_DATE = TO_DATE('" + dProcessDate.getValue().toString() + "','YYYY-MM-DD')),");
		sb.append("\n /*-- cte base con todas las transacciones consideradas dentro del calculo */");
		sb.append("\n /*-- CARTERA PARA PROJ_DATE = 0 */");
		sb.append("\n BASE_TRANSACTIONS AS (");
		sb.append("\n         SELECT");
		sb.append("\n T.PROCESS_DATE AS PROCESS_DATE");
		sb.append("\n         ,T.TRAN_DEAL");
		sb.append("\n         ,T.PORT_ID");
		sb.append("\n         ,I.INS_ID");
		sb.append("\n         ,ID.INSD_TICKER TICKER");
		sb.append("\n         ,CASE");
		sb.append("\n WHEN CLAS.CLAS_CONTABLE = 'A Financiar' THEN VV.RENDIMIENTO/100.00 ELSE YIELD.UTD_VALUE/100.00");
		sb.append("\n END AS TRAN_RATE");
		sb.append("\n         ,CLAS.CLAS_CONTABLE");
		sb.append("\n         ,CLAS.CLAS_RIESGO");
		sb.append("\n         ,CASE");
		sb.append("\n WHEN DT.UTD_VALUE <= LAST_DAY(T.PROCESS_DATE)  AND DT.UTD_VALUE >= TRUNC(T.PROCESS_DATE,'YEAR') THEN 3");
		sb.append("\n WHEN T.PORT_ID = "+EnumsPensionesPortfolios.PORVENIR.toInt()+" THEN 1");
		sb.append("\n ELSE 2");
		sb.append("\n END  RSCH_ID");
		sb.append("\n ,T.TRAN_CLOSE_POS AS TRAN_POSITION");
		sb.append("\n         ,T.TRAN_SETTLE_DATE");
		sb.append("\n         ,T.TRAN_TRADE_DATE");
		sb.append("\n FROM VCUBE.TRANSACTION T");
		sb.append("\n LEFT JOIN VCUBE.INSTRUMENT I ON ( I.INS_ID = T.INS_ID  AND I.PROCESS_DATE = T.PROCESS_DATE  )");
		sb.append("\n LEFT JOIN VCUBE.INS_DETAIL ID ON ( ID.INS_ID = T.INS_ID AND ID.PROCESS_DATE = T.PROCESS_DATE  )");
		sb.append("\n LEFT JOIN VCUBE.USER_TRAN_DEF_DOUBLE YIELD ON ( T.TRAN_ID = YIELD.TRAN_ID AND T.PROCESS_DATE = YIELD.PROCESS_DATE )");
		sb.append("\n LEFT JOIN VCUBE.USER_TRAN_DEF_DATE DT ON ( T.TRAN_ID = DT.TRAN_ID AND T.PROCESS_DATE = DT.PROCESS_DATE AND DT.UTD_ID  =  "+EnumsPensionesUserTranDef.UTD_PENS_TRAN_FECHA_RESOLUCION.toInt()+")");
		sb.append("\n LEFT JOIN VCUBE.BUY_SELL BS ON ( BS.BS_ID  = T.BS_ID )");
		sb.append("\n LEFT JOIN CLASIFICACIONES CLAS ON (T.TRAN_ID = CLAS.TRAN_ID AND T.PROCESS_DATE = CLAS.PROCESS_DATE )");
		sb.append("\n LEFT JOIN VCUBE_USER.VECTOR_VALMER VV ON (ID.INSD_TICKER = VV.INSTRUMENTO AND T.PROCESS_DATE = VV.PROCESS_DATE )");
		sb.append("\n LEFT JOIN VCUBE.ACCT_RESULT_VALUES ARV_MVALUE ON (T.TRAN_DEAL = ARV_MVALUE.TRAN_DEAL AND T.PROCESS_DATE = ARV_MVALUE.PROCESS_DATE AND ARV_MVALUE.ARTYPE_ID=20255)");
		sb.append("\n WHERE");
		sb.append("\n T.PROCESS_DATE = TO_DATE('" + dProcessDate.getValue().toString() + "','YYYY-MM-DD')");
		sb.append("\n AND I.PROD_ID = 5");
		sb.append("\n AND T.ASTT_ID = 2");
		sb.append("\n AND YIELD.UTD_ID = "+EnumsPensionesUserTranDef.UTD_PRICING_YIELD.toInt()+"");
		sb.append("\n AND T.PORT_ID IN  ("+EnumsPensionesPortfolios.IMSS.toInt()+","+EnumsPensionesPortfolios.ISSSTE.toInt()+","+EnumsPensionesPortfolios.PORVENIR.toInt()+")");
		sb.append("\n AND BS.BS_ID = 0");
		sb.append("\n AND ID.INSD_TICKER NOT IN ('97_MXMACFW_07-4U')");
		sb.append("\n ),");
		sb.append("\n ");
		sb.append("\n /****CTE - BLOTTER DE OPERACIONES: OBTIENE LAS OPERACIONES Y LA INFORMACIÓN PARA VALORIZAR****/");
		sb.append("\n BLT_OPERACIONES AS (");
		sb.append("\n         SELECT");
		sb.append("\n ROW_NUMBER() OVER (ORDER BY TRADE_DATE, TRAN_SETTLE_DATE) AS ID_OPERACION");
		sb.append("\n     , BO.PORTFOLIO");
		sb.append("\n         , BO.CLASIFICACION_CONTABLE");
		sb.append("\n         , BO.CLASIFICACION_RIESGO");
		sb.append("\n         , BO.TICKER");
		sb.append("\n         , BO.STOCK");
		sb.append("\n         , BO.BUY_SELL");
		sb.append("\n         , BO.TRADE_DATE");
		sb.append("\n         , BO.TRAN_SETTLE_DATE");
		sb.append("\n         , BO.YIELD/100.00 AS TRAN_RATE");
		sb.append("\n     , BO.PROCESS_DATE");
		sb.append("\n         , ID.INS_ID");
		sb.append("\n         , C.CCY_NAME");
		sb.append("\n         , YC.YLDCMP_NAME");
		sb.append("\n         , DCC.DCC_NAME");
		sb.append("\n         , NVL(RPU.UDI_VALUE,0.0) AS PRECIO_SUCIO"); //v2.1 JVG-20230215: Modificacion a UDI Proyectada
		sb.append("\n FROM VCUBE_USER.BLOTTER_OPERACIONES BO");
		sb.append("\n LEFT JOIN VCUBE.INS_DETAIL ID ON (BO.PROCESS_DATE = ID.PROCESS_DATE AND BO.TICKER = ID.INSD_TICKER)");
		sb.append("\n LEFT JOIN VCUBE.INSTRUMENT I ON (ID.PROCESS_DATE = I.PROCESS_DATE AND ID.INS_ID  = I.INS_ID)");
		sb.append("\n LEFT JOIN VCUBE.CURRENCY C ON (I.BASE_CCY_ID = C.CCY_ID)");
		sb.append("\n LEFT JOIN VCUBE.YIELD_COMPOUNDING YC ON (ID.YLDCMP_ID = YC.YLDCMP_ID)");
		sb.append("\n LEFT JOIN VCUBE.SIDE SD ON (I.INS_ID = SD.INS_ID AND I.PROCESS_DATE = SD.PROCESS_DATE)");
		sb.append("\n LEFT JOIN VCUBE.DAY_COUNT_CONV DCC ON (SD.DCC_ID = DCC.DCC_ID)");
		sb.append("\n LEFT JOIN VCUBE_USER.RISK_PROYECCION_UDI RPU ON (RPU.PERIOD_DATE = BO.TRADE_DATE AND RPU.PROCESS_DATE = BO.PROCESS_DATE)"); //v2.1 JVG-20230215: Modificacion a UDI Proyectada
		sb.append("\n WHERE BO.PROCESS_DATE = TO_DATE('" + dProcessDate.getValue().toString() + "','YYYY-MM-DD') AND BO.BUY_SELL = 'Sell'");
		sb.append("\n ORDER BY TRADE_DATE, TRAN_SETTLE_DATE, TICKER ");
		sb.append("\n ),");
		sb.append("\n /****CTE - CASHFOW DE LOS INSTRUMENTOS DEL BLOTTER + CALCULO DE DIAS CUPON Y PLAZO****/");
		sb.append("\n         CASHFLOWS_BLOTTER AS (");
		sb.append("\n                 SELECT");
		sb.append("\n         BO.*");
		sb.append("\n                 ,INTR.INT_FLOW");
		sb.append("\n                 ,INTR.INT_START_DATE");
		sb.append("\n                 ,INTR.INT_END_DATE");
		sb.append("\n                 ,INTR.INT_PAYMENT_DATE");
		sb.append("\n                 ,INTR.INT_END_DATE - BO.TRADE_DATE AS DAYS /*SE PUEDE USAR EL BO.TRADE_DATE EN CASO DE QUE SE REQUIERA EL CALCULO POR ESTA FECHA (ENVEJECIMIENTO)*/");
		sb.append("\n    ,INTR.INT_END_DATE - INTR.INT_START_DATE AS COUPON_DAYS");
		sb.append("\n    ,INTR.INT_AMOUNT + NVL(CF.CF_AMOUNT, 0) TOTAL_AMOUNT");
		sb.append("\n         FROM BLT_OPERACIONES BO");
		sb.append("\n         LEFT JOIN VCUBE.INTEREST INTR ON  (BO.INS_ID = INTR.INS_ID AND INTR.PROCESS_DATE = BO.PROCESS_DATE)");
		sb.append("\n         LEFT JOIN VCUBE.CFLOW CF ON (BO.INS_ID = CF.INS_ID AND INTR.INT_PAYMENT_DATE = CF.CF_PAYMENT_DATE AND CF.PROCESS_DATE = BO.PROCESS_DATE)");
		sb.append("\n         WHERE INTR.INT_END_DATE > BO.TRADE_DATE /*SE PUEDE USAR EL BO.TRADE_DATE EN CASO DE QUE SE REQUIERA EL CALCULO POR ESTA FECHA (ENVEJECIMIENTO)*/");
		sb.append("\n ),");
		sb.append("\n /****CTE - CALCULO DEL DISCOUNT FACTOR****/");
		sb.append("\n         DF_BLOTTER AS (SELECT");
		sb.append("\n CS.*");
		sb.append("\n         ,CASE");
		sb.append("\n WHEN YLDCMP_NAME = 'Semi-Annual (182/360)' THEN 1.0 / POWER ( 1.0 + TRAN_RATE * COUPON_DAYS / 360,  DAYS  / COUPON_DAYS )");
		sb.append("\n WHEN YLDCMP_NAME = 'Quarterly (91/360)' THEN 1.0 / POWER ( 1.0 + TRAN_RATE * COUPON_DAYS / 360,  DAYS / COUPON_DAYS  )");
		sb.append("\n WHEN YLDCMP_NAME = 'Monthly' THEN 1.0 / POWER ( 1.0 + TRAN_RATE * COUPON_DAYS / 360,  DAYS / COUPON_DAYS )");
		sb.append("\n WHEN YLDCMP_NAME = 'None' AND SUBSTR(TICKER,1,2) IN ('SP','SC') THEN 1.0 / ( 1.0 + TRAN_RATE * DAYS / 360)");
		sb.append("\n ELSE -1000000.0");
		sb.append("\n END DF");
		sb.append("\n FROM CASHFLOWS_BLOTTER CS)");
		sb.append("\n SELECT");
		sb.append("\n         PROCESS_DATE");
		sb.append("\n , TRADE_DATE AS PROJ_DATE");
		sb.append("\n             , ID_OPERACION AS TRAN_DEAL");
		sb.append("\n , PORTFOLIO AS PORT_ID");
		sb.append("\n             , INS_ID");
		sb.append("\n             , TICKER");
		sb.append("\n             , TRAN_RATE");
		sb.append("\n             , CLASIFICACION_CONTABLE AS CLAS_CONTABLE");
		sb.append("\n , CLASIFICACION_RIESGO AS CLAS_RIESGO");
		sb.append("\n             , 3 AS RSCH_ID");  
		sb.append("\n , CASE WHEN BUY_SELL = 'Sell' then DECODE(SUM(TOTAL_AMOUNT*DF*PRECIO_SUCIO),0,0.0,-TRUNC(STOCK/SUM(TOTAL_AMOUNT*DF*PRECIO_SUCIO),0)) ELSE DECODE(SUM(TOTAL_AMOUNT*DF*PRECIO_SUCIO),0,0.0,TRUNC(STOCK/SUM(TOTAL_AMOUNT*DF*PRECIO_SUCIO),0)) END AS TRAN_POSITION");
		sb.append("\n             , TRAN_SETTLE_DATE");
		sb.append("\n     FROM DF_BLOTTER");
		sb.append("\n     GROUP BY");
		sb.append("\n     PROCESS_DATE");
		sb.append("\n             , TRADE_DATE");
		sb.append("\n             , ID_OPERACION");
		sb.append("\n             , PORTFOLIO");
		sb.append("\n             , INS_ID");
		sb.append("\n             , TICKER");
		sb.append("\n             , TRAN_RATE");
		sb.append("\n             , CLASIFICACION_CONTABLE");
		sb.append("\n             , CLASIFICACION_RIESGO");
		sb.append("\n             , 3");
		sb.append("\n             , STOCK");
		sb.append("\n             , TRAN_SETTLE_DATE");
		sb.append("\n             , BUY_SELL");
		sb.append("\n     ORDER BY TICKER, PROJ_DATE");
	
//		 VMetrixPopup.createWarning().withLongMessage("SQL\n" + sb.toString()).withOkButton().open();
		 
		return sb.toString();
		
}
	
	private String getVentasTransacctionsPEPS(){
		//v2.0 JVG-20230105: Obtener transacciones PEPS, para quitar posicion. 
		StringBuilder sb = new StringBuilder();
		
			sb.append("\n  WITH  CLASIFICACIONES AS(");
            sb.append("\n  SELECT");
            sb.append("\n  A.PROCESS_DATE");
            sb.append("\n          ,A.TRAN_ID");
            sb.append("\n          ,A.UTD_VALUE CLAS_CONTABLE");
            sb.append("\n          ,B.UTD_VALUE CLAS_RIESGO");
            sb.append("\n  FROM VCUBE.USER_TRAN_DEF_STRING A");
            sb.append("\n  LEFT JOIN VCUBE.USER_TRAN_DEF_STRING B ON (A.TRAN_ID = B.TRAN_ID AND A.PROCESS_DATE = B.PROCESS_DATE  AND B.UTD_ID = "+EnumsPensionesUserTranDef.UTD_PENS_TRAN_CLASIF_RIESGO.toInt()+")");
            sb.append("\n  WHERE A.UTD_ID = "+EnumsPensionesUserTranDef.UTD_PENS_TRAN_CLASIF_CONTABLE.toInt()+"	AND A.PROCESS_DATE = TO_DATE('" + dProcessDate.getValue().toString() + "','YYYY-MM-DD')),");
            sb.append("\n  /*-- cte base con todas las transacciones consideradas dentro del calculo */");
            sb.append("\n  /*-- CARTERA PARA PROJ_DATE = 0 */");
            sb.append("\n  INSTRUMENTOS_BLOTTER AS (");
            sb.append("\n          SELECT DISTINCT PROCESS_DATE, PORTFOLIO , CLASIFICACION_CONTABLE , CLASIFICACION_RIESGO , TICKER");
            sb.append("\n FROM VCUBE_USER.BLOTTER_OPERACIONES WHERE PROCESS_DATE = TO_DATE('" + dProcessDate.getValue().toString() + "','YYYY-MM-DD') AND BUY_SELL = 'Sell'");
            sb.append("\n   )");
            sb.append("\n  SELECT");
            sb.append("\n  T.PROCESS_DATE AS PROCESS_DATE");
            sb.append("\n          ,T.TRAN_DEAL");
            sb.append("\n          ,T.PORT_ID");
            sb.append("\n          ,I.INS_ID");
            sb.append("\n          ,ID.INSD_TICKER TICKER");
            sb.append("\n          ,CASE");
            sb.append("\n  WHEN CLAS.CLAS_CONTABLE = 'A Financiar' THEN VV.RENDIMIENTO/100.00 ELSE YIELD.UTD_VALUE/100.00");
            sb.append("\n  END AS TRAN_RATE");
            sb.append("\n          ,CLAS.CLAS_CONTABLE");
            sb.append("\n          ,CLAS.CLAS_RIESGO");
            sb.append("\n          ,CASE");
            sb.append("\n  WHEN DT.UTD_VALUE <= LAST_DAY(T.PROCESS_DATE)  AND DT.UTD_VALUE >= TRUNC(T.PROCESS_DATE,'YEAR') THEN 3");
            sb.append("\n  WHEN T.PORT_ID = "+EnumsPensionesPortfolios.PORVENIR.toInt()+" THEN 1");
            sb.append("\n  ELSE 2");
            sb.append("\n  END  RSCH_ID");
            sb.append("\n   ,T.TRAN_CLOSE_POS AS TRAN_POSITION");
            sb.append("\n          ,T.TRAN_SETTLE_DATE");
            sb.append("\n          ,T.TRAN_TRADE_DATE");
            sb.append("\n          , BO.TICKER AS TICKER_BLOTTER");
            sb.append("\n  FROM VCUBE.TRANSACTION T");
            sb.append("\n  LEFT JOIN VCUBE.INSTRUMENT I ON ( I.INS_ID = T.INS_ID  AND I.PROCESS_DATE = T.PROCESS_DATE  )");
            sb.append("\n  LEFT JOIN VCUBE.INS_DETAIL ID ON ( ID.INS_ID = T.INS_ID AND ID.PROCESS_DATE = T.PROCESS_DATE  )");
            sb.append("\n  LEFT JOIN VCUBE.USER_TRAN_DEF_DOUBLE YIELD ON ( T.TRAN_ID = YIELD.TRAN_ID AND T.PROCESS_DATE = YIELD.PROCESS_DATE )");
            sb.append("\n  LEFT JOIN VCUBE.USER_TRAN_DEF_DATE DT ON ( T.TRAN_ID = DT.TRAN_ID AND T.PROCESS_DATE = DT.PROCESS_DATE AND DT.UTD_ID  = 20236)");
            sb.append("\n  LEFT JOIN VCUBE.BUY_SELL BS ON ( BS.BS_ID  = T.BS_ID )");
            sb.append("\n  LEFT JOIN CLASIFICACIONES CLAS ON (T.TRAN_ID = CLAS.TRAN_ID AND T.PROCESS_DATE = CLAS.PROCESS_DATE )");
            sb.append("\n  LEFT JOIN VCUBE_USER.VECTOR_VALMER VV ON (ID.INSD_TICKER = VV.INSTRUMENTO AND T.PROCESS_DATE = VV.PROCESS_DATE )");
            sb.append("\n  LEFT JOIN INSTRUMENTOS_BLOTTER BO ON (T.PROCESS_DATE = BO.PROCESS_DATE AND T.PORT_ID = BO.PORTFOLIO  AND ID.INSD_TICKER = BO.TICKER AND CLAS.CLAS_RIESGO = BO.CLASIFICACION_RIESGO AND CLAS.CLAS_CONTABLE = BO.CLASIFICACION_CONTABLE)");
            sb.append("\n  WHERE");
            sb.append("\n  T.PROCESS_DATE = TO_DATE('" + dProcessDate.getValue().toString() + "','YYYY-MM-DD')");
            sb.append("\n  AND I.PROD_ID = 5");
            sb.append("\n  AND T.ASTT_ID = 2");
            sb.append("\n  AND YIELD.UTD_ID = "+EnumsPensionesUserTranDef.UTD_PRICING_YIELD.toInt()+"");
            sb.append("\n  AND T.PORT_ID IN ("+EnumsPensionesPortfolios.IMSS.toInt()+","+EnumsPensionesPortfolios.ISSSTE.toInt()+","+EnumsPensionesPortfolios.PORVENIR.toInt()+")");
            sb.append("\n  AND BS.BS_ID = 0");
            sb.append("\n  AND ID.INSD_TICKER NOT IN ('97_MXMACFW_07-4U')");
            sb.append("\n  AND BO.TICKER IS NOT NULL");
            sb.append("\n  ORDER BY 5, 12, 2");
            
//            VMetrixPopup.createWarning().withLongMessage("SQL\n" + sb.toString()).withOkButton().open();
		
		return sb.toString();
		
		}
	

    private static Double getValueDbl(DTO dto, String sColumn){
        Optional<String> optAux = Optional.ofNullable(dto.getString(sColumn));
        return Double.parseDouble(optAux.orElse("0.0"));
    }
    private static String getValueStr(DTO dto, String sColumn){
        Optional<String> optAux = Optional.ofNullable(dto.getString(sColumn));
        return optAux.orElse("null");
    }
  //v2.0 JVG-20230105: Funcion que genera los resultados de las ventas por PEPS, para proyectarlas. 
    private void saveResults(List<DTO> resultados_proyeccion_ventas){

         DatabaseBO database = DatabaseBO.getMainInstance();

        String sSqlDel = "DELETE FROM VCUBE_USER.BLOTTER_OPERACIONES_VENTAS_RESULTADOS WHERE PROCESS_DATE = TO_DATE('" + dProcessDate.getValue().toString() + "','YYYY-MM-DD')";

        try {
            database.executeCommand(sSqlDel, null );

        } catch (DatabaseBOException e) {
            throw new RuntimeException("Error: saveResults(), Unable to Delete Old Values ::: " + e.getMessage());
        }


        List<String> lstInserts = resultados_proyeccion_ventas.stream().map(d ->
                "INSERT INTO VCUBE_USER.BLOTTER_OPERACIONES_VENTAS_RESULTADOS (PROCESS_DATE,TRAN_DEAL,PORT_ID,INS_ID,TICKER,TRAN_RATE, CLAS_CONTABLE,CLAS_RIESGO,RSCH_ID,TRAN_POSITION,TRAN_SETTLE_DATE,TRAN_TRADE_DATE,TICKER_BLOTTER,REMANENTE,NEW_POSITION,POSITION_FINAL,PROJ_DATE)" +
                        " VALUES (DATE '" + d.getString("PROCESS_DATE").replace(" 00:00:00.0","")+"'," + d.getString("TRAN_DEAL") + "," + d.getString("PORT_ID") + "," + d.getString("INS_ID") + ",'" + d.getString("TICKER") + "'," + d.getString("TRAN_RATE") + ",'"+ d.getString("CLAS_CONTABLE") + "','" + d.getString("CLAS_RIESGO") + "'," + d.getString("RSCH_ID") + "," + d.getString("TRAN_POSITION") + ", DATE '"+ d.getString("TRAN_SETTLE_DATE").replace(" 00:00:00.0","") + "', DATE '"+ d.getString("TRAN_TRADE_DATE").replace(" 00:00:00.0","") + "','"+ d.getString("TICKER_BLOTTER") + "',"+ d.getString("REMANENTE") + ","+ d.getString("NEW_POSITION") + ","+ d.getString("POSITION_FINAL") + ", DATE '"+ d.getString("PROJ_DATE").replace(" 00:00:00.0","") + "')"
        ).collect(Collectors.toList());


        try {

            for(String sSql : lstInserts)
                database.executeCommand(sSql, null );

        } catch (DatabaseBOException e) {
            throw new RuntimeException("Error: saveNav(), Unable to insert row on table ::: " + e.getMessage());
        }
       

    }
  //v2.0 JVG-20230105: Proyeccion de las ventas de Blotter
    private String getVentasBlotterProyection(){

    	
    	
    	StringBuilder sb = new StringBuilder();
    	
    	sb.append("\n WITH P_DATES AS (");
    	sb.append("\n  SELECT ");
    	sb.append("\n  	PERIOD_DATE ");
    	sb.append("\n  FROM TABLE(VCUBE_USER.PENS.TVF_PROJ_PERIOD(TO_DATE('" + dProcessDate.getValue().toString() + "','YYYY-MM-DD'))) WHERE PERIOD_DATE > TO_DATE('" + dProcessDate.getValue().toString() + "','YYYY-MM-DD') ");
    	sb.append("\n  fetch first " + k_proy.getValue().toString() + " - 1 rows only");
    	sb.append("\n  ),");
    	sb.append("\n ");
    	sb.append("\n BASE_TRANSACTIONS_T1 AS (	");
    	sb.append("\n SELECT ");
    	sb.append("\n  TRAN_DEAL");
    	sb.append("\n , BO.PROCESS_DATE");
    	sb.append("\n , TO_CHAR(BO.PORT_ID) AS PORTFOLIO");
    	sb.append("\n , C.CCY_NAME");
    	sb.append("\n , ID.INS_ID");
    	sb.append("\n , BO.TICKER ");
    	sb.append("\n , VV.TV AS TIPO_VALOR");
    	sb.append("\n , VV.EMISORA ");
    	sb.append("\n , VV.SERIE ");
    	sb.append("\n , 'Trading' AS T");
    	sb.append("\n , BO.TRAN_TRADE_DATE AS F_ULT_COMPRA");
    	sb.append("\n , BO.TRAN_RATE AS TASA_COMPRA");
    	sb.append("\n , BO.CLAS_CONTABLE ");
    	sb.append("\n , BO.TRAN_RATE");
    	sb.append("\n , VV.INTERES_DEV AS INTERES_CPN");
    	sb.append("\n , VV.TASA_CUPON_VIGENTE AS TASA_CUPON ");
    	sb.append("\n , VV.FECHA_FIN_CUPON   ");
    	sb.append("\n , VV.FECHA_INICIO_CUPON ");
    	sb.append("\n , BO.CLAS_RIESGO ");
    	sb.append("\n , 0 AS STOCK");
    	sb.append("\n , 'Sell' AS BUY_SELL ");
    	sb.append("\n , BO.TRAN_SETTLE_DATE ");
    	sb.append("\n , YC.YLDCMP_NAME ");
    	sb.append("\n , DCC.DCC_NAME ");
    	sb.append("\n , MON.PRECIO_SUCIO AS DIVISA");
    	sb.append("\n , VV.PRECIO_SUCIO");
    	sb.append("\n , VV.PLAZO");
    	sb.append("\n , VV.DIAS_VENC DXV");
    	sb.append("\n , BO.PROCESS_DATE + VV.DIAS_VENC AS F_VTO");
    	sb.append("\n , BO.NEW_POSITION AS TRAN_POSITION");
    	sb.append("\n , BO.PROJ_DATE");
    	sb.append("\n FROM VCUBE_USER.BLOTTER_OPERACIONES_VENTAS_RESULTADOS BO");
    	sb.append("\n 		LEFT JOIN VCUBE.INS_DETAIL ID ON (BO.PROCESS_DATE = ID.PROCESS_DATE AND BO.TICKER = ID.INSD_TICKER)");
    	sb.append("\n 		LEFT JOIN VCUBE.INSTRUMENT I ON (ID.PROCESS_DATE = I.PROCESS_DATE AND ID.INS_ID  = I.INS_ID)");
    	sb.append("\n 		LEFT JOIN VCUBE.CURRENCY C ON (I.BASE_CCY_ID = C.CCY_ID)");
    	sb.append("\n 		LEFT JOIN VCUBE.YIELD_COMPOUNDING YC ON (ID.YLDCMP_ID = YC.YLDCMP_ID)");
    	sb.append("\n 		LEFT JOIN VCUBE.SIDE SD ON (I.INS_ID = SD.INS_ID AND I.PROCESS_DATE = SD.PROCESS_DATE)");
    	sb.append("\n 	    LEFT JOIN VCUBE.DAY_COUNT_CONV DCC ON (SD.DCC_ID = DCC.DCC_ID)");
    	sb.append("\n 	    LEFT JOIN VCUBE_USER.VECTOR_VALMER MON ON (BO.PROCESS_DATE = MON.PROCESS_DATE AND MON.INSTRUMENTO = concat(concat(concat('*C_MXP', C.CCY_NAME), '_'), C.CCY_NAME))");
    	sb.append("\n 	    LEFT JOIN VCUBE_USER.VECTOR_VALMER vv ON (BO.PROCESS_DATE = VV.PROCESS_DATE AND VV.INSTRUMENTO = BO.TICKER)");
    	sb.append("\n WHERE BO.PROCESS_DATE = TO_DATE('" + dProcessDate.getValue().toString() + "','YYYY-MM-DD')");
    	sb.append("\n ),");
    	sb.append("\n ");
    	sb.append("\n CASHFLOWS AS (");
    	sb.append("\n  SELECT");
    	sb.append("\n      T.*");
    	sb.append("\n      ,INTR.INT_FLOW");
    	sb.append("\n      ,INTR.INT_START_DATE");
    	sb.append("\n      ,INTR.INT_END_DATE");
    	sb.append("\n      ,INTR.INT_PAYMENT_DATE");
    	sb.append("\n      ,INTR.INT_AMOUNT + NVL(CF.CF_AMOUNT, 0) TOTAL_AMOUNT");
    	sb.append("\n      ,INTR.INT_NOTIONAL");
    	sb.append("\n  FROM BASE_TRANSACTIONS_T1 T");
    	sb.append("\n    	 LEFT JOIN VCUBE.INTEREST INTR ON  (T.INS_ID = INTR.INS_ID AND INTR.PROCESS_DATE = T.PROCESS_DATE)");
    	sb.append("\n 	 LEFT JOIN VCUBE.CFLOW CF ON (T.INS_ID = CF.INS_ID AND INTR.INT_PAYMENT_DATE = CF.CF_PAYMENT_DATE AND CF.PROCESS_DATE = T.PROCESS_DATE)");
    	sb.append("\n  WHERE");
    	sb.append("\n       INTR.INT_END_DATE > T.PROJ_DATE),");
    	sb.append("\n  ");
    	sb.append("\n       ");
    	sb.append("\n VP_DAYS AS (  ");
    	sb.append("\n  SELECT");
    	sb.append("\n      P.PERIOD_DATE");
    	sb.append("\n      ,C.*");
    	sb.append("\n      ,CASE ");
    	sb.append("\n      	WHEN DCC_NAME = '30/360' THEN 360 * (EXTRACT(YEAR FROM INT_END_DATE)-EXTRACT(YEAR FROM P.PERIOD_DATE)) + 30 * (EXTRACT(MONTH FROM INT_END_DATE)-EXTRACT(MONTH FROM P.PERIOD_DATE)) +  (DECODE(EXTRACT(DAY FROM INT_END_DATE),31,30,EXTRACT(DAY FROM INT_END_DATE))-DECODE(EXTRACT(DAY FROM P.PERIOD_DATE),31,30,EXTRACT(DAY FROM P.PERIOD_DATE))) ");
    	sb.append("\n      	ELSE INT_END_DATE - P.PERIOD_DATE ");
    	sb.append("\n      END ");
    	sb.append("\n      	AS DAYS ");
    	sb.append("\n      ,CASE ");
    	sb.append("\n      	WHEN DCC_NAME = '30/360' THEN 360 * (EXTRACT(YEAR FROM INT_END_DATE)-EXTRACT(YEAR FROM TRAN_SETTLE_DATE)) + 30 * (EXTRACT(MONTH FROM INT_END_DATE)-EXTRACT(MONTH FROM TRAN_SETTLE_DATE)) +  (DECODE(EXTRACT(DAY FROM INT_END_DATE),31,30,EXTRACT(DAY FROM INT_END_DATE))-DECODE(EXTRACT(DAY FROM TRAN_SETTLE_DATE),31,30,EXTRACT(DAY FROM TRAN_SETTLE_DATE)))");
    	sb.append("\n      	ELSE INT_PAYMENT_DATE - TRAN_SETTLE_DATE ");
    	sb.append("\n      END ");
    	sb.append("\n      AS DAYS_FROM_SETTLE");
    	sb.append("\n      ,CASE ");
    	sb.append("\n      	WHEN DCC_NAME = '30/360' THEN 360 * (EXTRACT(YEAR FROM INT_END_DATE)-EXTRACT(YEAR FROM INT_START_DATE)) + 30 * (EXTRACT(MONTH FROM INT_END_DATE)-EXTRACT(MONTH FROM INT_START_DATE)) +  (DECODE(EXTRACT(DAY FROM INT_END_DATE),31,30,EXTRACT(DAY FROM INT_END_DATE))-DECODE(EXTRACT(DAY FROM INT_START_DATE),31,30,EXTRACT(DAY FROM INT_START_DATE)))");
    	sb.append("\n      	ELSE INT_END_DATE - INT_START_DATE ");
    	sb.append("\n      END ");
    	sb.append("\n      AS COUPON_DAYS ");
    	sb.append("\n      ,CASE ");
    	sb.append("\n      	WHEN DCC_NAME = '30/360' THEN 360 * (EXTRACT(YEAR FROM PROCESS_DATE)-EXTRACT(YEAR FROM FECHA_INICIO_CUPON)) + 30 * (EXTRACT(MONTH FROM PROCESS_DATE)-EXTRACT(MONTH FROM FECHA_INICIO_CUPON)) +  (DECODE(EXTRACT(DAY FROM PROCESS_DATE),31,30,EXTRACT(DAY FROM PROCESS_DATE))-DECODE(EXTRACT(DAY FROM FECHA_INICIO_CUPON),31,30,EXTRACT(DAY FROM FECHA_INICIO_CUPON)))");
    	sb.append("\n      	ELSE PROCESS_DATE - FECHA_INICIO_CUPON ");
    	sb.append("\n      END  ");
    	sb.append("\n      AS INT_DEV_DAYS ");
    	sb.append("\n      ,CASE ");
    	sb.append("\n      	WHEN DCC_NAME = '30/360' THEN 360 * (EXTRACT(YEAR FROM INT_END_DATE)-EXTRACT(YEAR FROM F_ULT_COMPRA)) + 30 * (EXTRACT(MONTH FROM INT_END_DATE)-EXTRACT(MONTH FROM F_ULT_COMPRA)) +  (DECODE(EXTRACT(DAY FROM INT_END_DATE),31,30,EXTRACT(DAY FROM INT_END_DATE))-DECODE(EXTRACT(DAY FROM F_ULT_COMPRA),31,30,EXTRACT(DAY FROM F_ULT_COMPRA))) ");
    	sb.append("\n      	ELSE INT_END_DATE - F_ULT_COMPRA ");
    	sb.append("\n      END ");
    	sb.append("\n      	AS DAYS_FECHA_COMPRA");
    	sb.append("\n       ,CASE ");
    	sb.append("\n      	WHEN DCC_NAME = '30/360' THEN 360 * (EXTRACT(YEAR FROM INT_END_DATE)-EXTRACT(YEAR FROM PROCESS_DATE)) + 30 * (EXTRACT(MONTH FROM INT_END_DATE)-EXTRACT(MONTH FROM PROCESS_DATE)) +  (DECODE(EXTRACT(DAY FROM INT_END_DATE),31,30,EXTRACT(DAY FROM INT_END_DATE))-DECODE(EXTRACT(DAY FROM PROCESS_DATE),31,30,EXTRACT(DAY FROM PROCESS_DATE))) ");
    	sb.append("\n      	ELSE INT_END_DATE - PROCESS_DATE ");
    	sb.append("\n      END ");
    	sb.append("\n      	AS DAYS_PROCESS_DATE");
    	sb.append("\n  FROM P_DATES P");
    	sb.append("\n          LEFT JOIN CASHFLOWS C ON (C.INT_END_DATE > P.PERIOD_DATE)");
    	sb.append("\n  WHERE P.PERIOD_DATE < C.INT_END_DATE	");
    	sb.append("\n  ),");
    	sb.append("\n ");
    	sb.append("\n  ");
    	sb.append("\n VP AS (  ");
    	sb.append("\n      SELECT");
    	sb.append("\n         vp.*");
    	sb.append("\n       ,CASE");
    	sb.append("\n         WHEN YLDCMP_NAME = 'Semi-Annual (182/360)' THEN 1.0 / POWER ( 1.0 + TRAN_RATE * COUPON_DAYS / 360,  DAYS  / COUPON_DAYS )");
    	sb.append("\n         WHEN YLDCMP_NAME = 'Quarterly (91/360)' THEN 1.0 / POWER ( 1.0 + TRAN_RATE * COUPON_DAYS / 360,  DAYS / COUPON_DAYS  )");
    	sb.append("\n         WHEN YLDCMP_NAME = 'Monthly' THEN 1.0 / POWER ( 1.0 + TRAN_RATE * COUPON_DAYS / 360,  DAYS / COUPON_DAYS  )        ");
    	sb.append("\n         WHEN YLDCMP_NAME = 'None' AND TIPO_VALOR IN ('SP','SC') THEN 1.0 / POWER ( 1.0 + TRAN_RATE * DAYS_FROM_SETTLE / 360, DAYS / DAYS_FROM_SETTLE   )");
    	sb.append("\n      ELSE -1000000.0");
    	sb.append("\n      END DF");
    	sb.append("\n       ,CASE");
    	sb.append("\n         WHEN YLDCMP_NAME = 'Semi-Annual (182/360)' THEN 1.0 / POWER ( 1.0 + TRAN_RATE * COUPON_DAYS / 360,  DAYS_FECHA_COMPRA  / COUPON_DAYS )");
    	sb.append("\n         WHEN YLDCMP_NAME = 'Quarterly (91/360)' THEN 1.0 / POWER ( 1.0 + TRAN_RATE * COUPON_DAYS / 360,  DAYS_FECHA_COMPRA / COUPON_DAYS  )");
    	sb.append("\n         WHEN YLDCMP_NAME = 'Monthly' THEN 1.0 / POWER ( 1.0 + TRAN_RATE * COUPON_DAYS / 360,  DAYS_FECHA_COMPRA / COUPON_DAYS  )        ");
    	sb.append("\n         WHEN YLDCMP_NAME = 'None' AND TIPO_VALOR IN ('SP','SC') THEN 1.0 / POWER ( 1.0 + TRAN_RATE * DAYS_FROM_SETTLE / 360, DAYS_FECHA_COMPRA / DAYS_FROM_SETTLE   )");
    	sb.append("\n      ELSE -1000000.0");
    	sb.append("\n      END DF_FECHA_COMPRA");
    	sb.append("\n      ,CASE");
    	sb.append("\n         WHEN YLDCMP_NAME = 'Semi-Annual (182/360)' THEN 1.0 / POWER ( 1.0 + TRAN_RATE * COUPON_DAYS / 360,  DAYS_PROCESS_DATE  / COUPON_DAYS )");
    	sb.append("\n         WHEN YLDCMP_NAME = 'Quarterly (91/360)' THEN 1.0 / POWER ( 1.0 + TRAN_RATE * COUPON_DAYS / 360,  DAYS_PROCESS_DATE / COUPON_DAYS  )");
    	sb.append("\n         WHEN YLDCMP_NAME = 'Monthly' THEN 1.0 / POWER ( 1.0 + TRAN_RATE * COUPON_DAYS / 360,  DAYS_PROCESS_DATE / COUPON_DAYS  )        ");
    	sb.append("\n         WHEN YLDCMP_NAME = 'None' AND TIPO_VALOR IN ('SP','SC') THEN 1.0 / POWER ( 1.0 + TRAN_RATE * DAYS_FROM_SETTLE / 360, DAYS_PROCESS_DATE / DAYS_FROM_SETTLE   )");
    	sb.append("\n      ELSE -1000000.0");
    	sb.append("\n      END DF_PROCESS_DATE");
    	sb.append("\n   , CASE WHEN FECHA_FIN_CUPON = INT_END_DATE THEN TASA_CUPON * INT_NOTIONAL * INT_DEV_DAYS / 360.0 ELSE 0.0 END AS INTERES_CPN_YO ");
    	sb.append("\n  FROM VP_DAYS vp");
    	sb.append("\n  )");
    	sb.append("\n  ");
    	sb.append("\n  --SACAR LOS VALORES QUE SE NECESITEN");
    	sb.append("\n  SELECT  ");
    	sb.append("\n 	VP.PROCESS_DATE");
    	sb.append("\n 	 ,VP.TRAN_DEAL");
//    	sb.append("\n 	 ,VP.PORTFOLIO AS PORT_ID");
    	sb.append("\n 	 ,PF.PORT_NAME AS PORT_ID"); //JVG-27022023: UTILIZAR NOMBRE DE PORTAFOLIO
    	sb.append("\n 	 ,VP.CCY_NAME");
    	sb.append("\n 	 ,CASE WHEN VP.TIPO_VALOR IN ('SC','SP') THEN 'BONO CUPON CERO A DESCUENTO' ELSE 'BONO' END AS INSTRUMENTO");
    	sb.append("\n 	 ,VP.TIPO_VALOR");
    	sb.append("\n 	 ,VP.EMISORA");
    	sb.append("\n 	 ,VP.SERIE");
    	sb.append("\n 	 ,VP.T");
    	sb.append("\n 	 ,VP.F_ULT_COMPRA");
    	sb.append("\n 	 ,VP.F_VTO");
    	sb.append("\n 	 ,VP.PLAZO");
    	sb.append("\n 	 ,VP.DXV");
    	sb.append("\n 	 ,VP.TASA_COMPRA AS TASA_PACT");
    	sb.append("\n 	 ,VP.CLAS_CONTABLE AS C");
    	sb.append("\n 	 ,VP.TRAN_POSITION AS TITULOS");
    	sb.append("\n 	 ,(SUM(TOTAL_AMOUNT* DF_FECHA_COMPRA) * DIVISA + INTERES_CPN) * TRAN_POSITION AS COSTO");
    	sb.append("\n 	 ,SUM(TOTAL_AMOUNT* DF) * nvl(RPU.UDI_VALUE,0.0) AS PRECIO");
    	sb.append("\n 	 ,VP.TRAN_RATE AS TASA");
    	sb.append("\n 	 ,(SUM(TOTAL_AMOUNT* DF_PROCESS_DATE) * DIVISA + INTERES_CPN)* TRAN_POSITION AS VALOR ");
    	sb.append("\n 	 ,INTERES_CPN * TRAN_POSITION AS INTERES_CPN"); //JVG-27022023: MULTIPLICAR POR LA POSICION
    	sb.append("\n 	 ,SUM(TOTAL_AMOUNT* DF) * nvl(RPU.UDI_VALUE,0.0) * TRAN_POSITION AS TOTAL");
    	sb.append("\n  	,VP.TASA_CUPON AS TASA_CPN");
    	sb.append("\n  	,VP.FECHA_FIN_CUPON AS F_PROX_CUPON");
    	sb.append("\n  	, CASE WHEN VP.CLAS_RIESGO  = 'Reservas' THEN TRAN_POSITION ELSE 0.0 END AS TIT_TECNICAS");
    	sb.append("\n  	, CASE WHEN VP.CLAS_RIESGO  = 'Capital' THEN TRAN_POSITION ELSE 0.0 END AS TIT_CAPITAL");
    	sb.append("\n  	, CASE WHEN VP.CLAS_RIESGO  = 'Otros Pasivos' THEN TRAN_POSITION ELSE 0.0 END AS TIT_OTROS");
    	sb.append("\n  	,VP.PERIOD_DATE AS FECHA_CARTERA");
    	sb.append("\n   	, 'BLOTTER OPERACIONES' AS TIPO_CARTERA 	");
    	sb.append("\n   	, PROJ_DATE");
    	sb.append("\n  FROM VP VP ");
    	sb.append("\n 	LEFT JOIN VCUBE_USER.RISK_PROYECCION_UDI RPU ON (RPU.PERIOD_DATE = VP.PERIOD_DATE AND RPU.PROCESS_DATE = VP.PROCESS_DATE)");
    	sb.append("\n 	LEFT JOIN VCUBE.PORTFOLIO PF ON (VP.PORTFOLIO = PF.PORT_ID)"); //JVG-27022023: UTILIZAR NOMBRE DE PORTAFOLIO
    	sb.append("\n  WHERE VP.PERIOD_DATE >= VP.PROJ_DATE");
    	sb.append("\n  GROUP BY  ");
    	sb.append("\n 	VP.PROCESS_DATE");
    	sb.append("\n 	 ,VP.TRAN_DEAL");
//    	sb.append("\n 	 ,VP.PORTFOLIO");
    	sb.append("\n 	 ,PF.PORT_NAME"); //JVG-27022023: UTILIZAR NOMBRE DE PORTAFOLIO
    	sb.append("\n 	 ,VP.CCY_NAME");
    	sb.append("\n 	 ,CASE WHEN VP.TIPO_VALOR IN ('SC','SP') THEN 'BONO CUPON CERO A DESCUENTO' ELSE 'BONO' END");
    	sb.append("\n 	 ,VP.TIPO_VALOR");
    	sb.append("\n 	 ,VP.EMISORA");
    	sb.append("\n 	 ,VP.SERIE");
    	sb.append("\n 	 ,VP.T");
    	sb.append("\n 	 ,VP.F_ULT_COMPRA");
    	sb.append("\n 	 ,VP.F_VTO");
    	sb.append("\n 	 ,VP.PLAZO");
    	sb.append("\n 	 ,VP.DXV");
    	sb.append("\n 	 ,VP.TASA_COMPRA");
    	sb.append("\n 	 ,VP.CLAS_CONTABLE ");
    	sb.append("\n 	 ,VP.TRAN_POSITION ");
    	sb.append("\n 	 ,VP.TRAN_RATE");
    	sb.append("\n  	,VP.TASA_CUPON ");
    	sb.append("\n  	,VP.FECHA_FIN_CUPON ");
    	sb.append("\n  	, CASE WHEN VP.CLAS_RIESGO  = 'Reservas' THEN TRAN_POSITION ELSE 0.0 END ");
    	sb.append("\n  	, CASE WHEN VP.CLAS_RIESGO  = 'Capital' THEN TRAN_POSITION ELSE 0.0 END ");
    	sb.append("\n  	, CASE WHEN VP.CLAS_RIESGO  = 'Otros Pasivos' THEN TRAN_POSITION ELSE 0.0 END");
    	sb.append("\n  	,VP.PERIOD_DATE");
    	sb.append("\n  	,VP.DIVISA");
    	sb.append("\n  	,VP.INTERES_CPN");
    	sb.append("\n  	,RPU.UDI_VALUE");
    	sb.append("\n  	, PROJ_DATE");
    	sb.append("\n 	ORDER BY VP.TRAN_DEAL, VP.PERIOD_DATE");
    	
    	return sb.toString();
    	
    }
  //v2.0 JVG-20230105: Funcion para agrupar los resultados por ventas y por proyección. 
	private List<DTO> getGroupedResults(List <DTO> listOutputDTO){

		Map<String, DTO> resume = null;
		resume = listOutputDTO.stream()
		.collect(Collectors.groupingBy(d ->  d.getString("PROCESS_DATE") + " - " + d.getString("TRAN_DEAL") + " - " + d.getString("PORT_ID") + " - " + d.getString("CCY_NAME") + " - " + d.getString("INSTRUMENTO") + " - " + d.getString("TIPO_VALOR")  + " - " + d.getString("EMISORA")	 + " - " + d.getString("SERIE")	 + " - " + d.getString("T")	 + " - " + d.getString("F_ULT_COMPRA")	 + " - " + d.getString("F_VTO")	 + " - " + d.getString("PLAZO")	 + " - " + d.getString("DXV")	 + " - " + d.getString("TASA_PACT")	 + " - " + d.getString("C")	 + " - " + d.getString("PRECIO")	 + " - " + d.getString("TASA")	 + " - " + d.getString("INTERES_CPN")	 + " - " + d.getString("TASA_CPN")	 + " - " + d.getString("F_PROX_CUPON")	 + " - " + d.getString("TIT_CAPITAL")	 + " - " + d.getString("TIT_OTROS")	 + " - " + d.getString("FECHA_CARTERA")	
				,
				Collectors.collectingAndThen(
						
						
						Collectors.reducing( (a,b)-> {
							
						DTO dtoNew = new DTO();

						dtoNew.setValue("PROCESS_DATE", a.getString("PROCESS_DATE"));
						dtoNew.setValue("TRAN_DEAL", a.getString("TRAN_DEAL"));
						dtoNew.setValue("PORT_ID", a.getString("PORT_ID"));
						dtoNew.setValue("CCY_NAME", a.getString("CCY_NAME"));
						dtoNew.setValue("INSTRUMENTO", a.getString("INSTRUMENTO"));
						dtoNew.setValue("TIPO_VALOR", a.getString("TIPO_VALOR"));
						dtoNew.setValue("EMISORA", a.getString("EMISORA"));
						dtoNew.setValue("SERIE", a.getString("SERIE"));
						dtoNew.setValue("T", a.getString("T"));
						dtoNew.setValue("F_ULT_COMPRA", a.getString("F_ULT_COMPRA"));
						dtoNew.setValue("F_VTO", a.getString("F_VTO"));
						dtoNew.setValue("PLAZO", a.getString("PLAZO"));
						dtoNew.setValue("DXV", a.getString("DXV"));
						dtoNew.setValue("TASA_PACT", a.getString("TASA_PACT"));
						dtoNew.setValue("C", a.getString("C"));
						dtoNew.setValue("TITULOS", Double.valueOf(a.getString("TITULOS")) + Double.valueOf(b.getString("TITULOS")));
						dtoNew.setValue("COSTO", Double.valueOf(a.getString("COSTO")) + Double.valueOf(b.getString("COSTO"))); 
						dtoNew.setValue("PRECIO", a.getString("PRECIO"));
						dtoNew.setValue("TASA", a.getString("TASA"));
						dtoNew.setValue("VALOR", Double.valueOf(a.getString("VALOR")) + Double.valueOf(b.getString("VALOR"))); 
//						dtoNew.setValue("INTERES_CPN", a.getString("INTERES_CPN"));
						dtoNew.setValue("INTERES_CPN", Double.valueOf(a.getString("INTERES_CPN")) + Double.valueOf(b.getString("INTERES_CPN")));
						dtoNew.setValue("TOTAL", Double.valueOf(a.getString("TOTAL")) + Double.valueOf(b.getString("TOTAL"))); 
						dtoNew.setValue("TASA_CPN", a.getString("TASA_CPN"));
						dtoNew.setValue("F_PROX_CUPON", a.getString("F_PROX_CUPON"));
						dtoNew.setValue("TIT_TECNICAS", Double.valueOf(a.getString("TIT_TECNICAS")) + Double.valueOf(b.getString("TIT_TECNICAS")));
						dtoNew.setValue("TIT_CAPITAL", Double.valueOf(a.getString("TIT_CAPITAL")) + Double.valueOf(b.getString("TIT_CAPITAL")));
						dtoNew.setValue("TIT_OTROS", Double.valueOf(a.getString("TIT_OTROS")) + Double.valueOf(b.getString("TIT_OTROS")));
						dtoNew.setValue("FECHA_CARTERA", a.getString("FECHA_CARTERA"));
						
						return dtoNew;
						}),Optional::get
			)
		));

		return new ArrayList<DTO>(resume.values());
	}
	
}


