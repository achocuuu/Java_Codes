/**
 * ***********************************************************************
 * Autor   : Jaime Ignacio Valenzuela - VMetrix International Spa.
 * Version : v3.0
 * Fecha   : 14/03/2022.
 * Proyecto: Pensiones Profuturo - ReporteCarteras
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

import com.vmetrix.v3.custom.EnumPensiones.enums.EnumsPensionesPortfolios;
import com.vmetrix.v3.custom.proyector.ReporteCarteras.ReporteCarterasView;
import cl.vmetrix.vcube.environment.VCubeSystem;
import cl.vmetrix.vcube.environment.VCubeSystemException;
import cl.vmetrix.core.dto.DTO;
import cl.vmetrix.vaadin.components.popup.VMetrixPopup;
import cl.vmetrix.vcube.vcode.display.IDisplayReference;
import cl.vmetrix.vcube.vcode.types.IVCodeWindow;
import cl.vmetrix.vcube.vcode.types.VCodeException;
import cl.vmetrix.vcube.vcode.types.windowCode.VLayout;
import cl.vmetrix.vcube.vcode.types.windowCode.VLayout.Orientation;

public class ReporteCarterasPresenter implements IVCodeWindow {
	
	
	private IDisplayReference iDisplayReference;
	
	private ReporteCarterasView view;
	@Override
	public void setDisplayReference(IDisplayReference iDisplayReference) {
	    this.iDisplayReference = iDisplayReference;
	}
	
	@Override
	public VLayout getVisualComponent() throws VCodeException {
	    return VLayout.getInstance(Orientation.NONE);
	}
	
	public ReporteCarterasPresenter(ReporteCarterasView view) {
		
		this.view = view;
		this.view.btnCalculate.setClickAction(this::clickCalculateButton);
		
	}
	
	private void clickCalculateButton(){
		
		LocalDate ldProcessDate = view.dProcessDate.getValue();
		String k_proy = view.k_proy.getValue();
		
		List<HashMap<String, Object>> listOutput = null;
		List<HashMap<String, Object>> listOutputPerGan = null;
		List<DTO> ReporteCarterasDTO = ReporteCarterasService.getReporteCarteras(ldProcessDate, k_proy); 
		
		if(ReporteCarterasDTO.isEmpty()){
			VMetrixPopup.createError()
			.withCaption( "Error" )
			.withMessage( "No hay datos de cartera a la fecha" )
			.withOkButton()
			.open();
			
		} else{
			
			ReporteCarterasEngine.ventasPEPS(ldProcessDate);
			List<DTO> listOutputCarteraBlotterDTO = ReporteCarterasService.getReporteCarterasBlotter(ldProcessDate, k_proy);
			List<DTO> listOutputCarteraBlotterVentasDTO = ReporteCarterasService.getReporteCarterasBlotterVentas(ldProcessDate, k_proy); 
			//Si existe la tabla de blotter que se incluya en el reporte
			if(listOutputCarteraBlotterDTO != null)
				ReporteCarterasDTO.addAll(listOutputCarteraBlotterDTO);
			if(listOutputCarteraBlotterVentasDTO != null)
				ReporteCarterasDTO.addAll(listOutputCarteraBlotterVentasDTO);
			
			//TODO: Reporte Carteras Supuesto Egresos/Ingresos
		
			//Se agrupan resultados
			List<DTO> finalResult = getGroupedResults(ReporteCarterasDTO);

			listOutput = finalResult
					.stream().map(dto -> dto.getHashMap())
					.collect(Collectors.toList());
			
			List<DTO> reporteGananciaPerdida = ReporteCarterasService.getReporteGananciaPerdida(ldProcessDate);
			
			listOutputPerGan = reporteGananciaPerdida
					.stream().map(dto -> dto.getHashMap())
					.collect(Collectors.toList());
			
			//Setting de la grilla
			view.gridReporte.setItems(listOutput);
			view.gridPerGan.setItems(listOutputPerGan);
				
		}
	}
	
	private List<DTO> getGroupedResults(List <DTO> listOutputDTO){

		Map<String, DTO> resume = null;
		resume = listOutputDTO.stream()
		.collect(Collectors.groupingBy(d ->  d.getString("PROCESS_DATE").replace(" 00:00:00.0","") + " - " + d.getString("TRAN_DEAL") + " - " + d.getString("PORT_ID") + " - " + d.getString("CCY_NAME") + " - " + d.getString("INSTRUMENTO") + " - " + d.getString("TIPO_VALOR")  + " - " + d.getString("EMISORA")	 + " - " + d.getString("SERIE")	 + " - " + d.getString("T")	 + " - " + d.getString("F_ULT_COMPRA").replace(" 00:00:00.0","")	 + " - " + d.getString("F_VTO").replace(" 00:00:00.0","")	 + " - " + d.getString("PLAZO")	 + " - " + d.getString("DXV")	 + " - " + d.getString("TASA_PACT")	 + " - " + d.getString("C")	 + " - " + d.getString("PRECIO")	 + " - " + d.getString("TASA")	 + " - " + d.getString("TASA_CPN")	 + " - " + d.getString("F_PROX_CUPON").replace(" 00:00:00.0","")	 + " - " + d.getString("TIT_CAPITAL")	 + " - " + d.getString("TIT_OTROS")	 + " - " + d.getString("FECHA_CARTERA").replace(" 00:00:00.0","")	
				,
				Collectors.collectingAndThen(
						
						
						Collectors.reducing( (a,b)-> {
							
						DTO dtoNew = new DTO();

						dtoNew.setValue("PROCESS_DATE", a.getString("PROCESS_DATE").replace(" 00:00:00.0",""));
						dtoNew.setValue("TRAN_DEAL", a.getString("TRAN_DEAL"));
						dtoNew.setValue("PORT_ID", a.getString("PORT_ID"));
						dtoNew.setValue("CCY_NAME", a.getString("CCY_NAME"));
						dtoNew.setValue("INSTRUMENTO", a.getString("INSTRUMENTO"));
						dtoNew.setValue("TIPO_VALOR", a.getString("TIPO_VALOR"));
						dtoNew.setValue("EMISORA", a.getString("EMISORA"));
						dtoNew.setValue("SERIE", a.getString("SERIE"));
						dtoNew.setValue("T", a.getString("T"));
						dtoNew.setValue("F_ULT_COMPRA", a.getString("F_ULT_COMPRA").replace(" 00:00:00.0",""));
						dtoNew.setValue("F_VTO", a.getString("F_VTO").replace(" 00:00:00.0",""));
						dtoNew.setValue("PLAZO", a.getString("PLAZO"));
						dtoNew.setValue("DXV", a.getString("DXV"));
						dtoNew.setValue("TASA_PACT", a.getString("TASA_PACT"));
						dtoNew.setValue("C", a.getString("C"));
						dtoNew.setValue("TITULOS", Double.valueOf(a.getString("TITULOS")) + Double.valueOf(b.getString("TITULOS")));
						dtoNew.setValue("COSTO", Double.valueOf(a.getString("COSTO")) + Double.valueOf(b.getString("COSTO"))); 
						dtoNew.setValue("PRECIO", a.getString("PRECIO"));
						dtoNew.setValue("TASA", a.getString("TASA"));
						dtoNew.setValue("VALOR", Double.valueOf(a.getString("VALOR")) + Double.valueOf(b.getString("VALOR"))); 
						dtoNew.setValue("INTERES_CPN", Double.valueOf(a.getString("INTERES_CPN")) + Double.valueOf(b.getString("INTERES_CPN")));
						dtoNew.setValue("TOTAL", Double.valueOf(a.getString("TOTAL")) + Double.valueOf(b.getString("TOTAL"))); 
						dtoNew.setValue("TASA_CPN", a.getString("TASA_CPN"));
						dtoNew.setValue("F_PROX_CUPON", a.getString("F_PROX_CUPON").replace(" 00:00:00.0",""));
						dtoNew.setValue("TIT_TECNICAS", Double.valueOf(a.getString("TIT_TECNICAS")) + Double.valueOf(b.getString("TIT_TECNICAS")));
						dtoNew.setValue("TIT_CAPITAL", Double.valueOf(a.getString("TIT_CAPITAL")) + Double.valueOf(b.getString("TIT_CAPITAL")));
						dtoNew.setValue("TIT_OTROS", Double.valueOf(a.getString("TIT_OTROS")) + Double.valueOf(b.getString("TIT_OTROS")));
						dtoNew.setValue("FECHA_CARTERA", a.getString("FECHA_CARTERA").replace(" 00:00:00.0",""));
						
						return dtoNew;
						}),Optional::get
			)
		));

		return new ArrayList<DTO>(resume.values());
	}
	

}
