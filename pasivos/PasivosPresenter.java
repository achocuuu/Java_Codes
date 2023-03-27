package com.vmetrix.v3.custom.proyector.pasivos;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.vmetrix.v3.custom.EnumPensiones.enums.EnumsPensionesPortfolios;
import com.vmetrix.v3.custom.proyector.enums.EnumsProyectorRiskSchema;
import com.vmetrix.v3.custom.proyector.util.ProyectorUtil;

import cl.vmetrix.core.dto.DTO;
import cl.vmetrix.vaadin.components.popup.VMetrixPopup;
import cl.vmetrix.vcube.vcode.display.IDisplayReference;
import cl.vmetrix.vcube.vcode.types.IVCodeWindow;
import cl.vmetrix.vcube.vcode.types.VCodeException;
import cl.vmetrix.vcube.vcode.types.windowCode.VLayout;
import cl.vmetrix.vcube.vcode.types.windowCode.VLayout.Orientation;

public class PasivosPresenter implements IVCodeWindow   {
	
	private IDisplayReference	iDisplayReference;
	private PasivosView			view;
	boolean bCanSave			= false;

	@Override
	public void setDisplayReference(IDisplayReference iDisplayReference) {
	    this.iDisplayReference = iDisplayReference;
	}
	
	@Override
	public VLayout getVisualComponent() throws VCodeException {
	    return VLayout.getInstance(Orientation.NONE);
	}
	
	public PasivosPresenter(PasivosView view) {
		this.view = view;
		
		this.view.btnCalculate.setClickAction(this::clickCalculateButton);
		this.view.btnSave.setClickAction(this::clickSaveButton);
		this.view.btnGetHistorical.setClickAction(this::clickHistoricalButton);
	}
	
	private void clickCalculateButton(){
		
		LocalDate ldProcessDate = view.dProcessDate.getValue();
		
		// Set data on grids
			List<DTO> dtoData = setRefNames(PasivosService.getCalculatedData(ldProcessDate));
			
			
//			VMetrixPopup
//				.createInfo()
//				.withOkButton()
//				.withMessage(dtoData.toString())
//				.open();
//			
			
	
	    //Sort
		    Comparator<DTO> sortOrder = Comparator
		            .comparing((DTO dto) -> dto.getString("PROCESS_DATE"))
		            .thenComparing(dto -> dto.getString("PROJ_DATE"))
		            .thenComparing(dto -> dto.getString("PERIOD_DATE"))
		            .thenComparing(dto -> dto.getString("PORT_NAME"))
		            .thenComparing(dto -> dto.getString("RSCH_NAME"))
		            .thenComparing(dto -> dto.getString("RINP_NAME"));	 
		    
		    dtoData.sort(sortOrder);
		
		if(dtoData.isEmpty()){
				VMetrixPopup.createError()
				.withCaption( "Error" )
				.withMessage( "There's no data." )
				.withOkButton()
				.open();
				view.gridData.setItemsDTO(new ArrayList<DTO>());
	
		} else {
	
			// Set data on grid
	        view.gridData.setItemsDTO(dtoData);
	        
	        //Allow Save Button - only allowed after user used calculate button
	        bCanSave = true;
			view.btnSave.setStyle( "tiny secondary blue iconLeft fa-save marginMe-top");
		}
		
	}
	
	private void clickSaveButton(){
		
		if (bCanSave){
		
			LocalDate dSelectedProcessDate = view.dProcessDate.getValue();
			
			// Si ya existen datos históricos
			if(!PasivosService.getHistoricalData(dSelectedProcessDate).isEmpty()){
	
				// Reprocesar
				VMetrixPopup.createQuestion()
				.withCaption( "Reproceso" )
				.withCancelButton()
				.withTitleBar(true)
				.withMessage("Ya existe información guardada para:"
						+ "\n Process Date: "+dSelectedProcessDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
						+ "\nDesea continuar de todas formas?")
				.withYesButton(()->{
					
					// Borrar data anterior
						PasivosService.deletePreviousStoredData(dSelectedProcessDate);
						
					// Guardar
						if( !view.gridData.getItems().isEmpty() ){
							if(PasivosService.save(ProyectorUtil.hashMapToDTO((view.gridData.getItems())))){
								
								VMetrixPopup.createCheck()
								.withCaption( "Exito!" )
								.withMessage( "La información fue guardada exitosamente." )
								.withOkButton()
								.open();
								bCanSave = false;
								view.btnSave.setStyle( "tiny secondary blue iconLeft fa-save marginMe-top v-disabled");
							} else {
								VMetrixPopup.createError()
								.withCaption( "Error" )
								.withMessage( "Hubo un error al guardar la información." )
								.withOkButton()
								.open();
							}
						} else {
							VMetrixPopup.createError()
							.withCaption( "Error" )
							.withMessage( "No hay data en la grilla." )
							.withOkButton()
							.open();
						}
	
				}).open();
				
			} 
			// No existen datos históricos, guardar directamente
			else {
				// Save Data
				if( !view.gridData.getItems().isEmpty() ){
					if(PasivosService.save(ProyectorUtil.hashMapToDTO((view.gridData.getItems())))){
						
						VMetrixPopup.createCheck()
						.withCaption( "Exito!" )
						.withMessage( "La información fue guardada exitosamente." )
						.withOkButton()
						.open();
						bCanSave = false;
						view.btnSave.setStyle( "tiny secondary blue iconLeft fa-save marginMe-top v-disabled");
					} else {
						VMetrixPopup.createError()
						.withCaption( "Error" )
						.withMessage( "Hubo un error al guardar la información." )
						.withOkButton()
						.open();
					}
				} else {
					VMetrixPopup.createError()
					.withCaption( "Error" )
					.withMessage( "No hay data en la grilla." )
					.withOkButton()
					.open();
				}
	
			}
		}
		
	}
	
	private void clickHistoricalButton(){
		
		LocalDate ldProcessDate = view.dProcessDate.getValue();
		
		// Set data on grids
		List<DTO> dtoData = setRefNames(PasivosService.getHistoricalData(ldProcessDate));
		
	    //Sort
	    Comparator<DTO> sortOrder = Comparator
	            .comparing((DTO dto) ->   dto.getString("PROCESS_DATE"))
	            .thenComparing(dto -> dto.getString("PROJ_DATE"))
	            .thenComparing(dto -> dto.getString("PERIOD_DATE"))
	            .thenComparing(dto -> dto.getString("PORT_NAME"))
	            .thenComparing(dto -> dto.getString("RSCH_NAME"))
	            .thenComparing(dto -> dto.getString("RINP_NAME"));	 
	    
	    dtoData.sort(sortOrder);
		
		if(dtoData.isEmpty()){
				VMetrixPopup.createError()
				.withCaption( "Error" )
				.withMessage( "There's no data saved for selected process date." )
				.withOkButton()
				.open();
				view.gridData.setItems(new ArrayList<HashMap<String,Object>>());
	
		} else {
	
	        view.gridData.setItemsDTO(dtoData);
	        
		}
		
		// Cant save again the same data, so all getted saved data doesn't allow re-save
	    bCanSave = false;
		view.btnSave	.setStyle( "tiny secondary blue iconLeft fa-save marginMe-top v-disabled");
	
		
	}
	
	public static List<DTO> setRefNames(List<DTO> dtoData) {
	
	List<DTO> dtoInputs = PasivosService.getDataRiskInputs();
	
	dtoData.stream().forEach(dto->{
		int iPortId = Integer.valueOf(dto.getString("PORT_ID"));
		int iRschId = Integer.valueOf(dto.getString("RSCH_ID"));
		int iRinpId = Integer.valueOf(dto.getString("RINP_ID"));
		
		String sInputName = dtoInputs.stream().filter(inp-> inp.getString("RINP_ID").equals(String.valueOf(iRinpId))).collect(Collectors.toList()).get(0).getString("RINP_NAME");
		
		dto.setValue("PORT_NAME", EnumsPensionesPortfolios.getEnumById(iPortId).toString());
		dto.setValue("RSCH_NAME", EnumsProyectorRiskSchema.getEnumById(iRschId).toString());
		dto.setValue("RINP_NAME", sInputName);
	});
	return dtoData;
}

}