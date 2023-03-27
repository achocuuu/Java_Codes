package com.vmetrix.v3.custom.proyector.pasivos;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.vmetrix.v3.custom.proyector.util.ProyectorUtil;

import cl.vmetrix.core.dto.DTO;

public class PasivosEngine {
	
	static String sDatabaseTableName = "VCUBE_USER.RISK_PROYECTOR_PASIVOS_PROCESSED";
	
	static boolean save (List<DTO> dtoData) {
    	return ProyectorUtil.saveDataPerRounds(sDatabaseTableName, dtoData, 1000);
	}	
	
	static boolean deletePreviousStoredData (LocalDate ldProcessDate) {
    	return ProyectorUtil.deletePreviousStoredData(sDatabaseTableName,ldProcessDate);
	}
	
    static List<DTO> getCalculatedData(LocalDate ldProcessDate){
    	
    	Map<String, DTO> resume = null;

    	// Get Dates
    	    List<DTO> dtoFechasProyeccion	= PasivosService.getFechasProyeccion(ldProcessDate);
    	    
    	   if(!dtoFechasProyeccion.isEmpty()){
    		   
    		   
		   LocalDate ldFirstProjDate = ldProcessDate.plusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
    	
//    	    LocalDate ldFirstProjDate		= LocalDate.parse(dtoFechasProyeccion.get(1).getString("PROJ_DATE").replace(" 00:00:00.0",""), DateTimeFormatter.ofPattern("yyyy-MM-dd"));	    
    	    LocalDate ldEOY					= ldProcessDate.plusYears(-1).with(TemporalAdjusters.lastDayOfYear());   
    	    LocalDate ldLastDayFebraury		= LocalDate.of(ldProcessDate.getYear(),2,1).with(TemporalAdjusters.lastDayOfMonth());
    	    LocalDate ldNextFrebraury		= ldLastDayFebraury.isAfter(ldProcessDate) ? ldLastDayFebraury : ldLastDayFebraury.plusYears(1).with(TemporalAdjusters.lastDayOfMonth());

    	// Get Data
    	    List<DTO> dtoPasivosAll			= PasivosService.getDataPasivos(ldProcessDate,ldEOY,ldNextFrebraury,ldFirstProjDate);
    	    List<DTO> dtoInflacionAll		= PasivosService.getDataInflacion(ldProcessDate,ldEOY);

        //Tratamiento comienza en i=1, es decir con T=1 para proyectar los pasivos.
    	    for (int i = 1; i < dtoFechasProyeccion.size(); i ++) {
    	    	
    	        List<DTO> dtoPasivosProj = new ArrayList<DTO>();

    	        String sProjectDate		 	= dtoFechasProyeccion.get(i).getString("PROJ_DATE").replace(" 00:00:00.0","");
                LocalDate ldProjectDate = LocalDate.parse(sProjectDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

    	        
    	        String sProjectDateAnt		= dtoFechasProyeccion.get(i-1).getString("PROJ_DATE").replace(" 00:00:00.0","");

    	        List<DTO> dtoPasivosAnt		= dtoPasivosAll.stream().filter(dto -> dto.getValue("PROJ_DATE").toString().replace(" 00:00:00.0", "").equals(sProjectDateAnt)).collect(Collectors.toList());
    	        List<DTO> dtoInflacion		= dtoInflacionAll.stream().filter(dto -> dto.getValue("PROJ_DATE").toString().replace(" 00:00:00.0", "").equals(sProjectDate)).collect(Collectors.toList());
    	        
    	        double dInflacionMensual = Double.parseDouble(dtoInflacion.get(0).getValue("INFLACION_MENSUAL").toString());

    	        //Tratamiento de T anterior, pasandolo a T siguiente (Inflacionando)
    	        for (DTO dtoRow : dtoPasivosAnt) {

    	            LocalDate ldPeriodDate = LocalDate.parse(dtoRow.getValue("PERIOD_DATE").toString().replace(" 00:00:00.0", ""), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    	            int iInputId = Integer.parseInt(dtoRow.getValue("RINP_ID").toString());
    	            
    	            double dNewValue = 0.0;

    	            if ((iInputId == 6 || iInputId == 7 || iInputId == 33) && ldPeriodDate.isBefore(ldNextFrebraury)) {
    	            	dNewValue = ldPeriodDate.isBefore(ldProjectDate) ? 0.0 : Double.parseDouble(dtoRow.getValue("RPAS_VALUE").toString());
    	            } else {
    	            	dNewValue = ldPeriodDate.isBefore(ldProjectDate) ? 0.0 : Double.parseDouble(dtoRow.getValue("RPAS_VALUE").toString()) * dInflacionMensual;
    	            }

    	            DTO dtoNew = new DTO();
    	            dtoNew.setValue("PROCESS_DATE", dtoRow.getValue("PROCESS_DATE").toString().replace(" 00:00:00.0", ""));
    	            dtoNew.setValue("PROJ_DATE", sProjectDate );
    	            dtoNew.setValue("PERIOD_DATE", dtoRow.getValue("PERIOD_DATE").toString().replace(" 00:00:00.0", ""));
    	            dtoNew.setValue("PORT_ID", dtoRow.getValue("PORT_ID").toString());
    	            dtoNew.setValue("RSCH_ID", dtoRow.getValue("RSCH_ID").toString());
    	            dtoNew.setValue("RINP_ID", dtoRow.getValue("RINP_ID").toString());
    	            dtoNew.setValue("RPAS_VALUE", dNewValue);
    	            dtoPasivosProj.add(dtoNew);
    	        }

    	        //Se agregan los nuevos valores del T-1 inflacionados a T
    	        dtoPasivosAll.addAll(dtoPasivosProj);
        }
        
    	    
    		 resume = dtoPasivosAll.stream()
    				 .collect(Collectors.groupingBy(d ->  d.getString("PROCESS_DATE") + " - " + d.getString("PROJ_DATE") + " - " + d.getString("PERIOD_DATE") + " - " + d.getString("PORT_ID") + " - " + d.getString("RSCH_ID") + " - " + d.getString("RINP_ID")
    				 		,
    						 Collectors.collectingAndThen(
    								 
    								 
    								 Collectors.reducing( (a,b)-> {
    									 
    						    	  DTO dtoNew = new DTO();

    						    	  dtoNew.setValue("PROCESS_DATE", a.getString("PROCESS_DATE"));
    						    	  dtoNew.setValue("PROJ_DATE", a.getString("PROJ_DATE"));
    						    	  dtoNew.setValue("PERIOD_DATE", a.getString("PERIOD_DATE"));
    						    	  dtoNew.setValue("PORT_ID", a.getString("PORT_ID"));
    						    	  dtoNew.setValue("RSCH_ID", a.getString("RSCH_ID"));
    						    	  dtoNew.setValue("RINP_ID", a.getString("RINP_ID"));	
    						    	  dtoNew.setValue("RPAS_VALUE", Double.valueOf(a.getString("RPAS_VALUE")) + Double.valueOf(b.getString("RPAS_VALUE")) );
    								  
    								  return dtoNew;
    								 }),Optional::get
    						)
    					));

    		 return new ArrayList<DTO>(resume.values());

    		 
    	}
    	    
    	    return new ArrayList<DTO>();
    	
    }

    static List<DTO> getHistoricalData(LocalDate currentDate){
    	return PasivosRepository
    			.qHistoricalData
    			.withValue("I_PROCESS_DATE", currentDate)
    			.getListDTO();
    }

	static List<DTO> getFechasProyeccion(LocalDate ldProcessDate) {
		return PasivosRepository
				.qProjectionDates
				.withValue("I_PROCESS_DATE", ldProcessDate)
				.getListDTO();
	}

	static List<DTO> getDataPasivos(LocalDate ldProcessDate, LocalDate ldEOY, LocalDate ldNextFrebraury,
			LocalDate ldFirstProjDate) {
		return PasivosRepository
				.qInsumosPasivos
				.withValue("I_PROCESS_DATE", ldProcessDate)
				.withValue("I_FIRST_PROJECT_DATE", ldFirstProjDate)
				.withValue("I_NEXT_FEBRUARY", ldNextFrebraury)
				.withValue("I_EOY_DATE", ldEOY)
				.getListDTO();
	}

	static List<DTO> getDataInflacion(LocalDate ldProcessDate, LocalDate ldEOY) {
		return PasivosRepository
				.qInflation
				.withValue("I_PROCESS_DATE", ldProcessDate)
				.withValue("I_EOY_DATE", ldEOY)
				.getListDTO();
	}

	static List<DTO> getDataRiskInputs() {
		return PasivosRepository
				.qInputs
				.getListDTO();
	}
	
	PasivosEngine () {

	}

}
