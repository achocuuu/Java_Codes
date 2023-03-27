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

import cl.vmetrix.core.dto.DTO;

public interface ReporteCarterasService {
	
	static List<DTO> getReporteCarteras(LocalDate ldProcessDate, String k_proy){	
		return ReporteCarterasEngine.getReporteCarteras(ldProcessDate, k_proy);
	}
	
	static List<DTO> getBlotterVentas(LocalDate ldProcessDate){	
		return ReporteCarterasEngine.getBlotterVentas(ldProcessDate);
	}
	
	static List<DTO> getVentasTransactionsPEPS(LocalDate ldProcessDate){	
		return ReporteCarterasEngine.getVentasTransactionsPEPS(ldProcessDate);
	}
	
	static List<DTO> getReporteCarterasBlotter(LocalDate ldProcessDate, String k_proy){	
		return ReporteCarterasEngine.getReporteCarterasBlotter(ldProcessDate, k_proy);
	}
	
	static List<DTO> getReporteCarterasBlotterVentas(LocalDate ldProcessDate, String k_proy){	
		return ReporteCarterasEngine.getReporteCarterasBlotterVentas(ldProcessDate, k_proy);
	}
	
	static List<DTO> getReporteGananciaPerdida(LocalDate ldProcessDate){	
		return ReporteCarterasEngine.getReporteGananciaPerdida(ldProcessDate);
	}
	

}
