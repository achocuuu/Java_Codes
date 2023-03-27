package com.vmetrix.v3.custom.proyector.pasivos;
import java.time.LocalDate;
import java.util.List;

import cl.vmetrix.core.dto.DTO;

public interface PasivosService {
	
    static List<DTO> getCalculatedData(LocalDate ldProcessDate){
    	return PasivosEngine.getCalculatedData(ldProcessDate);
    }    
    
    static List<DTO> getHistoricalData(LocalDate ldProcessDate){
    	return PasivosEngine.getHistoricalData(ldProcessDate);        
    }

	static List<DTO> getFechasProyeccion(LocalDate ldProcessDate) {
    	return PasivosEngine.getFechasProyeccion(ldProcessDate);
	}

	static List<DTO> getDataPasivos(LocalDate ldProcessDate, LocalDate ldEOY, LocalDate ldNextFrebraury,
			LocalDate ldFirstProjDate) {
    	return PasivosEngine.getDataPasivos(ldProcessDate, ldEOY, ldNextFrebraury, ldFirstProjDate);
	}

	static List<DTO> getDataInflacion(LocalDate ldProcessDate, LocalDate ldEOY) {
    	return PasivosEngine.getDataInflacion(ldProcessDate, ldEOY);
	}
	
	static List<DTO> getDataRiskInputs() {
    	return PasivosEngine.getDataRiskInputs();
	}

	static boolean deletePreviousStoredData(LocalDate ldProcessDate) {
    	return PasivosEngine.deletePreviousStoredData(ldProcessDate);
	}

	static boolean save(List<DTO> dtoData) {
    	return PasivosEngine.save(dtoData);
	}
}