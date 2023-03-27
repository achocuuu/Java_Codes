/**
 * ***********************************************************************
 * Autor   : Jaime Ignacio Valenzuela - VMetrix International Spa.
 * Version : v3.0
 * Fecha   : 14/03/2022.
 * Proyecto: Pensiones Profuturo - ReporteCarteras 
*/

package com.vmetrix.v3.custom.proyector.ReporteCarteras;

import java.time.LocalDate;
import java.util.List;

import com.vmetrix.v3.custom.proyector.ReporteCarteras.ReporteCarterasRepository;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import com.vmetrix.v3.custom.EnumPensiones.enums.EnumsPensionesPortfolios;
import cl.vmetrix.core.dto.DTO;
import cl.vmetrix.core.log.VMetrixLogType;
import cl.vmetrix.core.log.VMetrixLogger;
import cl.vmetrix.vaadin.components.popup.VMetrixPopup;
import cl.vmetrix.persistencelayer.database.DatabaseBO;
import cl.vmetrix.persistencelayer.database.DatabaseBOException;

public class ReporteCarterasEngine {
	
	static void ventasPEPS(LocalDate ldProcessDate){
		// [JVG]: Obtiene los datos 
			List<DTO> blotter_ventas = getBlotterVentas(ldProcessDate);
			List<DTO> ventas_transacctions_PEPS = getVentasTransactionsPEPS(ldProcessDate);
			List<DTO> resultado_proyeccion_ventas = new ArrayList<DTO>();
			List<DTO> ventas_ticker = new ArrayList<DTO>();
	        for (int i = 0; i < blotter_ventas.size(); i ++){
	
	            DTO elementoAnt_Blotter = i == 0 ? null : blotter_ventas.get(i - 1);
	            DTO elemento_Blotter = blotter_ventas.get(i);
	         
	            String ticker = elemento_Blotter.getValue("TICKER").toString();
	            String proj_date = elemento_Blotter.getValue("PROJ_DATE").toString();
	            String ticker_anterior = i == 0 ? "" :getValueStr(elementoAnt_Blotter, "TICKER");
	            double tran_position_sell = getValueDbl(elemento_Blotter,"TRAN_POSITION");
	           if (ticker.equals(ticker_anterior)){ //[JVG]: En caso de que pasamos a otro proj_date del mismo ticker, quedarse con la selección anterior
	        	   
	               ventas_ticker = ventas_ticker.stream().filter(dto-> !dto.getValue("TRAN_POSITION").equals(0.0)).collect(Collectors.toList());
	           
	           }else{
	            	// [JVG]: Obtiene las transacciones a las cuales descontar por las ventas
	                ventas_ticker = ventas_transacctions_PEPS.stream().filter(dto-> dto.getValue("TICKER").equals(ticker)).collect(Collectors.toList());
	
	            }
	
	            double remanente = 0.0;
	            double new_position = 0.0;
	            double position_cumulative = 0.0;	     
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
	
	                //[JVG]: Guardar resultado de new position para el proj date. Se crea nuevo DTO para que las listas no apunten a una misma referencia. 
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
	        saveResults(resultado_proyeccion_ventas, ldProcessDate);
	
	}

	private static Double getValueDbl(DTO dto, String sColumn){
	        Optional<String> optAux = Optional.ofNullable(dto.getString(sColumn));
	        return Double.parseDouble(optAux.orElse("0.0"));
	    }
	 
    private static String getValueStr(DTO dto, String sColumn){
        Optional<String> optAux = Optional.ofNullable(dto.getString(sColumn));
        return optAux.orElse("null");
    }

	
	 private static void saveResults(List<DTO> resultados_proyeccion_ventas, LocalDate ldProcessDate){

        DatabaseBO database = DatabaseBO.getMainInstance();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
               
        String dProcessDate = ldProcessDate.format(formatter);
        
        String sSqlDel = "DELETE FROM VCUBE_USER.BLOTTER_OPERACIONES_VENTAS_RESULTADOS WHERE PROCESS_DATE = TO_DATE('" + dProcessDate + "','YYYY-MM-DD')";

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
            throw new RuntimeException("Error: saveResults(), Unable to insert row on table ::: " + e.getMessage());
        }
       
    }
	 
	static List<DTO> getReporteCarteras(LocalDate ldProcessDate, String k_proy) {
			
			int k = Integer.parseInt(k_proy);
			
			return ReporteCarterasRepository.qReporteCarteras
					.withValue("I_PROCESS_DATE", ldProcessDate)
					.withValue("K_PROY",k)
					.getListDTO();		
	}
	
	static List<DTO> getBlotterVentas(LocalDate ldProcessDate) {
	
			return ReporteCarterasRepository.qBlotterVentas
					.withValue("I_PROCESS_DATE", ldProcessDate)
					.getListDTO();		
		}
	
	static List<DTO> getVentasTransactionsPEPS(LocalDate ldProcessDate) {
		
		return ReporteCarterasRepository.qVentasTransactionsPEPS
				.withValue("I_PROCESS_DATE", ldProcessDate)
				.getListDTO();		
	}

	static List<DTO> getReporteCarterasBlotter(LocalDate ldProcessDate, String k_proy) {
		
		int k = Integer.parseInt(k_proy);
		
		return ReporteCarterasRepository.qReporteCarterasBlotter
				.withValue("I_PROCESS_DATE", ldProcessDate)
				.withValue("K_PROY",k)
				.getListDTO();		
}
	
static List<DTO> getReporteCarterasBlotterVentas(LocalDate ldProcessDate, String k_proy) {
		
		int k = Integer.parseInt(k_proy);
		
		return ReporteCarterasRepository.qReporteCarterasBlotterVentas
				.withValue("I_PROCESS_DATE", ldProcessDate)
				.withValue("K_PROY",k)
				.getListDTO();		
}

static List<DTO> getReporteGananciaPerdida(LocalDate ldProcessDate) {
	
	return ReporteCarterasRepository.qReporteGananciaPerdida
			.withValue("I_PROCESS_DATE", ldProcessDate)
			.getListDTO();
}
	

}
