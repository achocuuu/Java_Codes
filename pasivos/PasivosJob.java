/*
 * ***********************************************************************
 * Autor   : Basthian Matthews - VMetrix International Spa.
 * Fecha   : Junio 2021.
 * Proyecto: Pensiones Profuturo - Proyector
 * 
 * History ***************************************************************
 * Autor      :
 * Fecha      :
 * Descripcion: 
 */
package com.vmetrix.v3.custom.proyector.pasivos;
import java.time.LocalDate;
import java.util.List;

import com.vmetrix.v3.custom.commons.util.V3System;
import com.vmetrix.v3.custom.proyector.activos.ActivosService;

import cl.vmetrix.core.dto.DTO;
import cl.vmetrix.core.log.VMetrixLogType;
import cl.vmetrix.vcube.vcode.types.IVCodeScheduledJob;
import cl.vmetrix.venvironment.ModulesCodes;
import cl.vmetrix.venvironment.logs.bo.VCubeLogBO;

public class PasivosJob implements IVCodeScheduledJob {
	
	private final String REPORT_NAME = "PROYECTOR_PASIVOS";
			
	@Override
	public void execute() {		

		VCubeLogBO.log(VMetrixLogType.DEBUG, this.getClass(), ModulesCodes.MODULE_VSCHEDULER_CODE, "System Job","", "Starting Job: " + REPORT_NAME, null);
		LocalDate ldNow	= V3System.getSystemDate();
		
		try {
			
			List<DTO> dtoData = PasivosService.getCalculatedData(ldNow);

			if(!dtoData.isEmpty()){
				
				if(!PasivosService.getHistoricalData(ldNow).isEmpty()){
					PasivosService.deletePreviousStoredData(ldNow);
					PasivosService.save(dtoData);
				} else {
					PasivosService.save(dtoData);
				}
			}

		} catch (Throwable e) {
			VCubeLogBO.log(VMetrixLogType.ERROR, this.getClass(), ModulesCodes.MODULE_VSCHEDULER_CODE, "System Job","", "Error In Job: " + REPORT_NAME, e);
		}

		VCubeLogBO.log(VMetrixLogType.DEBUG, this.getClass(), ModulesCodes.MODULE_VSCHEDULER_CODE, "System Job","", "Ending Job: " + REPORT_NAME, null);}

}
