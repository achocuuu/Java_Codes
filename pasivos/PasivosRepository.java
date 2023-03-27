package com.vmetrix.v3.custom.proyector.pasivos;

import com.vmetrix.v3.custom.commons.db.ColumnType;
import com.vmetrix.v3.custom.commons.db.Parameter;
import com.vmetrix.v3.custom.commons.db.Query;

public interface PasivosRepository {

    /**
     * Returns passive historical data
     * @parameter I_PROCESS_DATE specific date
     */
    Query qHistoricalData = Query.withSQL(
    	     String.join(" \n ",
            		"select",
            		"    TO_CHAR(h.PROCESS_DATE, 'YYYY-MM-DD') PROCESS_DATE",
            		"    ,TO_CHAR(h.PROJ_DATE, 'YYYY-MM-DD') PROJ_DATE   ",
            		"    ,TO_CHAR(h.PERIOD_DATE, 'YYYY-MM-DD') PERIOD_DATE ",
            		"    ,h.PORT_ID      ",
            		"    ,h.RSCH_ID      ",
            		"    ,h.RINP_ID      ",
            		"    ,h.RPAS_VALUE",
            		"from VCUBE_USER.RISK_PROYECTOR_PASIVOS_PROCESSED h",
            		"where h.process_date = :I_PROCESS_DATE",
            		"order by",
            		"    h.PROJ_DATE    ",
            		"    ,h.PERIOD_DATE  ",
            		"    ,h.PORT_ID      ",
            		"    ,h.RSCH_ID      ",
            		"    ,h.RINP_ID      "
    	    )
    ).withParameter(Parameter.of("I_PROCESS_DATE", ColumnType.ORACLE_DATE));

    /**
     * Returns passive data inputs
     * @parameter I_PROCESS_DATE specific date
    	    		 "          ,:I_FIRST_PROJECT_DATE as first_proj_date",
    	    		 "          ,:I_NEXT_FEBRUARY as next_february_date",
    	    		 "          ,:I_EOY_DATE eoy_date",
     */
    Query qInsumosPasivos = Query.withSQL(
    	     String.join(" \n ",
    	    		 "with",
    	    		 "parameters as (",
    	    		 "     select",
    	    		 "          :I_PROCESS_DATE as process_date",
    	    		 "          ,:I_FIRST_PROJECT_DATE as first_proj_date",
    	    		 "          ,:I_NEXT_FEBRUARY as next_february_date",
    	    		 "          ,:I_EOY_DATE eoy_date",
    	    		 "     from dual",
    	    		 "),",
    	    		 "",
    	    		 "udi_eoy as (",
    	    		 "     select",
    	    		 "          a.process_date",
    	    		 "          ,a.precio_sucio udi_t",
    	    		 "     from vcube_user.vector_valmer a",
    	    		 "          left join parameters params on ( 1 = 1 )",
    	    		 "     where a.instrumento = '*C_MXPUDI_UDI'",
    	    		 "          and a.process_date = params.eoy_date",
    	    		 "),",
    	    		 "",
    	    		 "proy_udi as (",
    	    		 "     select",
    	    		 "          rpu.period_date",
    	    		 "          ,rpu.udi_value",
    	    		 "     from vcube_user.risk_proyeccion_udi rpu",
    	    		 "          left join parameters params on ( 1 = 1 )",
    	    		 "     where rpu.process_date = params.process_date",
    	    		 "     order by rpu.period_date",
    	    		 "),",
    	    		 "",
    	    		 "inflacion_anual AS (",
    	    		 "     select",
    	    		 "          a.period_date proj_date",
    	    		 "          ,a.udi_value / udi_eoy.udi_t inflacion_anual",
    	    		 "     from proy_udi a",
    	    		 "          left join udi_eoy udi_eoy on ( 1 = 1 )",
    	    		 ")",
    	    		 "",
    	    		 "select",
    	    		 "     TO_CHAR(rp.process_date, 'YYYY-MM-DD') process_date",
    	    		 "     ,TO_CHAR(rp.proj_date, 'YYYY-MM-DD') proj_date",
    	    		 "     ,TO_CHAR(rp.period_date, 'YYYY-MM-DD') period_date",
    	    		 "     ,rp.port_id",
    	    		 "     ,rp.rsch_id",
    	    		 "     ,rp.rinp_id",
    	    		 "     ,nvl(case",
    	    		 "          when rp.rinp_id in (6,33) and rp.proj_date >= params.first_proj_date and period_date >= params.next_february_date",
    	    		 "          then rp.rpas_value * inflacion_anual",
    	    		 "          else rp.rpas_value",
    	    		 "     end,0.0) as rpas_value", //JVG-29112022: Control de nulos query
    	    		 "from vcube_user.risk_pasivos rp",
    	    		 "     left join parameters params on (1=1)",
    	    		 "     left join inflacion_anual inf on rp.proj_date = inf.proj_date",
    	    		 "     left join portfolio p on p.port_id = rp.port_id ",
    	    		 "     left join vcube_user.risk_schema rs on rs.rsch_id = rp.rsch_id ",
    	    		 "     left join vcube_user.risk_input_def rid on rid.rinp_id = rp.rinp_id ",
    	    		 "where rp.process_date = params.process_date",
    	    		 "     and rp.rsch_id in (1,2,3)",
    	    		 "     and rp.rinp_id in (1,2,4,5,6,7,33,9,41)",
    	    		 "order by 1,2,3,4,5,6"

    	    		 ))
    		.withParameter(Parameter.of("I_PROCESS_DATE", ColumnType.ORACLE_DATE))
    		.withParameter(Parameter.of("I_FIRST_PROJECT_DATE", ColumnType.ORACLE_DATE))
    		.withParameter(Parameter.of("I_NEXT_FEBRUARY", ColumnType.ORACLE_DATE))
    		.withParameter(Parameter.of("I_EOY_DATE", ColumnType.ORACLE_DATE));
	
    /**
     * Returns possible projection dates based on process date of inputs
     * @parameter I_PROCESS_DATE specific date
     */
    Query qProjectionDates = Query.withSQL(
    	     String.join(" \n ",
    	    		 
    	    		 "select distinct",
    	    		 "	TO_CHAR(rp.proj_date, 'YYYY-MM-DD') proj_date",
    	    		 "from vcube_user.risk_pasivos rp",
    	    		 "where rp.process_date = :I_PROCESS_DATE",
    	    		 "	and rp.rsch_id in (1,2,3)",
    	    		 "	and rp.rinp_id in (1,2,4,5,6,7,33,9,41)",
    	    		 "order by TO_CHAR(rp.proj_date, 'YYYY-MM-DD')"
    	    )
    ).withParameter(Parameter.of("I_PROCESS_DATE", ColumnType.ORACLE_DATE));	
    
    /**
     * Returns inflation data
     * @parameter I_PROCESS_DATE specific date
     * @parameter I_EOY_DATE End Of Year Date
     */
    Query qInflation = Query.withSQL(
    	     String.join(" \n ",
    	    		 
    	    		 "with",
    	    		 "parameters as (",
    	    		 "     select",
    	    		 "          :I_PROCESS_DATE as process_date",
    	    		 "          ,:I_EOY_DATE AS eoy_date",
    	    		 "     from dual",
    	    		 "),",
    	    		 "",
    	    		 "udi_eoy as (",
    	    		 "     select",
    	    		 "          a.process_date",
    	    		 "          ,a.precio_sucio udi_t",
    	    		 "     from vcube_user.vector_valmer a",
    	    		 "          left join parameters params on (1=1)",
    	    		 "     where a.instrumento = '*C_MXPUDI_UDI'",
    	    		 "          and a.process_date = params.eoy_date",
    	    		 "),",
    	    		 "",
    	    		 "proy_udi as (",
    	    		 "     select",
    	    		 "          rpu.period_date",
    	    		 "          ,rpu.udi_value",
    	    		 "     from vcube_user.risk_proyeccion_udi rpu",
    	    		 "          left join parameters params on (1=1)",
    	    		 "     where rpu.process_date = params.process_date",
    	    		 "     order by 1",
    	    		 ")",
    	    		 "select",
    	    		 "    a.period_date proj_date",
    	    		 "    , NVL( a.udi_value / lag(a.udi_value,1) over (order by a.period_date),1.0)  inflacion_mensual",
    	    		 "    , NVL(a.udi_value / udi_eoy.udi_t,0.0) inflacion_anual", //JVG-29112022: Control de nulos query
    	    		 "from proy_udi a",
    	    		 "     left join udi_eoy udi_eoy on (1=1)"
    	    		 ))
    		.withParameter(Parameter.of("I_PROCESS_DATE", ColumnType.ORACLE_DATE))
    		.withParameter(Parameter.of("I_EOY_DATE", ColumnType.ORACLE_DATE));
    
    /**
     * Returns inputs data
     */
    Query qInputs = Query.withSQL(
    	     String.join(" \n ",
    	    		 "select",
    	    		 "	i.RINP_ID",
    	    		 "	,i.RINP_NAME",
    	    		 "from vcube_user.risk_input_def i"
    	    )
    );	
    
    
    
}
