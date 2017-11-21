package com.isesol.mes.ismes.em.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.google.common.collect.Lists;
import com.isesol.ismes.platform.core.service.bean.Dataset;
import com.isesol.ismes.platform.module.Bundle;
import com.isesol.ismes.platform.module.Parameters;
import com.isesol.ismes.platform.module.Sys;
import com.isesol.ismes.platform.module.bean.File;
import com.isesol.mes.ismes.em.constant.SqlConstant;
import com.isesol.mes.ismes.em.constant.TableConstant;
import com.isesol.mes.ismes.em.util.DateTimeUtils;

public class EquipmentManagementService {
	
	private Logger log4j = Logger.getLogger(EquipmentManagementService.class);

	/**服务_查询设备信息  id集合 查询设备集合
	 * @param parameters
	 * @param bundle
	 */
	public void query_sbxxList(Parameters parameters, Bundle bundle) {
		Dataset dataset_sbxx = Sys.query(TableConstant.设备信息表,"sbid,sbbh,sbmc,sblxid,ccbh,ip,cj,zzjgid,jyrq,sbwz,kfid", "sbid in "+parameters.get("val_sb").toString(), null,new Object[]{} );
		bundle.put("sbxx",  dataset_sbxx.getList());
	}
	public void query_sbxxListbysbwz(Parameters parameters, Bundle bundle) {
		String query_sbwz = parameters.getString("query_sbwz"); //  物料名称
		String con = "1 = 1 ";
		List<Object> val = new ArrayList<Object>();
		if(StringUtils.isNotBlank(query_sbwz)) 
		{
			con = con + " and sbwz like ? ";
			val.add("%"+query_sbwz+"%");
		}
		Dataset dataset_sbxx = Sys.query(TableConstant.设备信息表,"sbid,sbbh,sbmc,sblxid,ccbh,ip,cj,zzjgid,jyrq,sbwz,kfid", con, null,val.toArray() );
		bundle.put("sbxx",  dataset_sbxx.getList());
	}
	/**
	 * 通过设备位置查询加工单元信息和设备信息
	 * @param parameters
	 * @param bundle
	 */
	public void query_jgdyandsbxxbysbwz(Parameters parameters, Bundle bundle){
		String query_sbwz = parameters.getString("query_sbwz"); //  设备位置
		String con = "1 = 1 ";
		List<Object> val = new ArrayList<Object>();
		if(StringUtils.isNotBlank(query_sbwz)) 
		{
			con = con + " and sbwz like ? ";
			val.add("%"+query_sbwz+"%");
		}
		Dataset dataset = Sys.query(new String[] {"em_jgdyb", "em_jgdysbglb", "em_sbxxb" },
				"em_jgdyb left join em_jgdysbglb on em_jgdyb.jgdyid = em_jgdysbglb.jgdyid left join em_sbxxb on em_jgdysbglb.sbid = em_sbxxb.sbid ",
				"jgdybh sbbh,jgdymc sbmc,jgdyms sbms,bdgkjbh,sbwz,em_sbxxb.sbid,em_jgdyb.jgdyid,em_sbxxb.sblxid,em_sbxxb.ccbh,em_sbxxb.ip,em_sbxxb.cj,em_sbxxb.zzjgid,em_sbxxb.jyrq,em_sbxxb.kfid", 
				con, null, val.toArray());
		bundle.put("jgdyandsbxx",  dataset.getList());
	}
	
	public void query_sbxxListBysbbh(Parameters parameters, Bundle bundle) {
		Dataset dataset_sbxx = Sys.query(TableConstant.设备信息表,"sbid,sbbh,sbbh id,sbmc,sbmc title,sblxid,ccbh,ip,cj,zzjgid,jyrq,sbwz,kfid", "sbbh in "+parameters.get("val_sb").toString(), null,new Object[]{} );
		bundle.put("sbxx",  dataset_sbxx.getList());
	}
	
	/**
	 * 服务_查询设备信息  id 查询设备
	 * @param parameters
	 * @param bundle
	 */
	public void query_sbxxInfo(Parameters parameters, Bundle bundle) {
		StringBuffer conditionSb = new StringBuffer(" 1 = 1 ");
		List<Object> conditionValues = new ArrayList<Object>();
		String sbid = parameters.getString("sbid");
		if(StringUtils.isNotBlank(sbid)){
			conditionSb = conditionSb.append(" and sbid = ? ");
			conditionValues.add(sbid);
		}
		List<String> sbidList = (List<String>) parameters.get("sbidList");
		if(CollectionUtils.isNotEmpty(sbidList)){
			StringBuffer sbid_sb = new StringBuffer();
			for(String str : sbidList){
				sbid_sb = sbid_sb.append("'").append(str).append("',");
			}
			sbid_sb.deleteCharAt(sbid_sb.length() - 1);
			conditionSb = conditionSb.append("  and sbid in ( ").append(sbid_sb).append(" )");
		}
		String zzjgid = parameters.getString("zzjgid");
		if(StringUtils.isNotBlank(zzjgid)){
			conditionSb = conditionSb.append(" and zzjgid = ? ");
			conditionValues.add(zzjgid);
		}
		String sbbh = parameters.getString("sbbh");
		if(StringUtils.isNotBlank(sbbh)){
			conditionSb = conditionSb.append(" and sbbh = ? ");
			conditionValues.add(sbbh);
		}

		//TODO  其他的可以往里边加
		
		Boolean ifPage = parameters.getString("ifPage") == null ? false : Boolean.valueOf(parameters.getString("ifPage"));
		Dataset dataset_sbxx = null;
		if(ifPage){
			int page = parameters.get("page") == null ? 1 : parameters.getInteger("page");
			int pageSize = parameters.get("pageSize") == null ? 100 : parameters.getInteger("pageSize");
			dataset_sbxx = Sys.query(TableConstant.设备信息表,"sbid,sbmc,sblxid,ccbh,ip,cj,zzjgid,jyrq,sbwz,sbbh",
					conditionSb.toString(), null,(page-1)*pageSize, pageSize,conditionValues.toArray() );
			int totalPage = dataset_sbxx.getTotal() % pageSize== 0 ? dataset_sbxx.getTotal()/pageSize:
				dataset_sbxx.getTotal()/pageSize+1;
			bundle.put("totalPage", totalPage);
			bundle.put("currentPage", page);
			bundle.put("totalRecord", dataset_sbxx.getTotal());
			bundle.put("records", dataset_sbxx.getCount());
			bundle.put("rows",dataset_sbxx.getList());
		}else{
			dataset_sbxx = Sys.query(TableConstant.设备信息表,"sbid,sbmc,sblxid,ccbh,ip,cj,zzjgid,jyrq,sbwz,sbbh,sbtpid",
					conditionSb.toString(), null,conditionValues.toArray() );
		}
		bundle.put("sbxx",  dataset_sbxx.getMap());
		bundle.put("sbxxList",  dataset_sbxx.getList());
	}
	
	public void query_jgdyInfo(Parameters parameters, Bundle bundle) {
		String jgdyid = parameters.getString("sbid");
		String gkjIP = parameters.getString("gkjIP");
		String con = " 1 = 1 ";
		List<Object> val = new ArrayList<Object>();
		
		if(StringUtils.isNotBlank(jgdyid))
		{
			con = con + " and em_jgdyb.jgdyid =  ? ";
			val.add(jgdyid);
		}
		if(StringUtils.isNotBlank(gkjIP))
		{
			con = con + " and bdgkjbh = ? " ;
			val.add(gkjIP);
		}
		Dataset dataset_sbxx = Sys.query(new String[] {"em_jgdyb", "em_jgdysbglb", "em_sbxxb" },
				"em_jgdyb left join em_jgdysbglb on em_jgdyb.jgdyid = em_jgdysbglb.jgdyid left join em_sbxxb on em_jgdysbglb.sbid = em_sbxxb.sbid ",
				"em_jgdyb.jgdyid,jgdybh sbbh,jgdymc sbmc,jgdyms sbms,bdgkjbh,sbwz,em_sbxxb.sbid", con, null,	val.toArray()  );
		bundle.put("sbxxList",  dataset_sbxx.getList());
	}
	
	public void query_jgdysbglb(Parameters parameters, Bundle bundle){
		String jgdyid = parameters.getString("jgdyid");
		Dataset dataset_jgdysbglb = Sys.query("em_jgdysbglb", "glid,jgdyid,sbid",
				" jgdyid = ? ", null , new Object[]{jgdyid});
		bundle.put("sbidList",  dataset_jgdysbglb.getList());
	}
	
	/**
	 * 服务_查询设备信息  根据设备编号
	 * @param parameters
	 * @param bundle
	 */
	public void query_sbxxInfoBySbbh(Parameters parameters, Bundle bundle) {
		StringBuffer conditionSb = new StringBuffer();
		List<Object> conditionValues = new ArrayList<Object>();
	
		String sbbh = parameters.getString("sbbh");
		String sbmc = parameters.getString("sbmc");
		boolean flag = false;
		if(StringUtils.isNotBlank(sbbh)){
			conditionSb = conditionSb.append(" sbbh like ? ");
			conditionValues.add("%"+sbbh+"%");
			flag = true;
		}
		if(StringUtils.isNotBlank(sbmc)){
			if(flag){
				conditionSb.append(" and ");
			}
			conditionSb = conditionSb.append(" sbmc like ? ");
			conditionValues.add("%"+sbmc+"%");
		}
		
		Dataset dataset_sbxx = Sys.query(TableConstant.设备信息表, "sbid,sbmc,sblxid,ccbh,ip,cj,zzjgid,jyrq,sbwz,sbbh,sbtpid",
				conditionSb.toString(), null, conditionValues.toArray());
		bundle.put("sbxxList", dataset_sbxx.getList());
	}
	
	/**
	 * 服务 通过能力组id集合 查询能力组列表
	 * 多个id parameters key = nlzids  List<Map<String,Object>> idList = (List<Map<String, Object>>) parameters.get("nlzids");
	 * 单个id parameters key = nlzid  Map<String,Object> idMap = (Map<String, Object>) parameters.get("nlzid");
	 * @param parameters
	 * @param bundle
	 */
	public void query_nlzList(Parameters parameters, Bundle bundle){
		StringBuffer conditionSb = new StringBuffer(" 1 = 1 ");
		List<Object> conditionValue = new ArrayList<Object>();
		List<Map<String,Object>> idList = (List<Map<String, Object>>) parameters.get("nlzids");
		if(CollectionUtils.isNotEmpty(idList)){
			StringBuffer sb = new StringBuffer("'");
			for(Map<String,Object> map : idList){
				if(map.get("nlzid") == null || StringUtils.isBlank( map.get("nlzid").toString())){
					continue;
				}
				sb = sb.append(map.get("nlzid").toString()).append("','");
			}
			if(sb.toString().endsWith(",'")){
				sb = sb.delete(sb.length() - 2, sb.length());
			}
			conditionSb.append(" and nlzid in (").append(sb).append(")");
		}
		Map<String,Object> idMap = (Map<String, Object>) parameters.get("nlzid");
		if(MapUtils.isNotEmpty(idMap)){
			String nlzid = idMap.get("nlzid").toString();
			conditionSb.append(" and nlzid = ? ");
			conditionValue.add(nlzid);
		}
		
		//能力组编号
		String nlzid =  parameters.getString("nlzidStr");
		if(StringUtils.isNotBlank(nlzid)){
			conditionSb.append(" and nlzid =  ? ");
			conditionValue.add(nlzid );
		}
		
		//能力组编号
		String nlzbh =  parameters.getString("nlzbh");
		if(StringUtils.isNotBlank(nlzbh)){
			conditionSb.append(" and nlzbh like  ? ");
			conditionValue.add("%" + nlzbh + "%");
		}
		String nlzbh_eq =  parameters.getString("nlzbh_eq");
		if(StringUtils.isNotBlank(nlzbh_eq)){
			conditionSb.append(" and nlzbh =  ? ");
			conditionValue.add(nlzbh_eq);
		}
		
		//能力组名称
		String nlzmc =  parameters.getString("nlzmc");
		if(StringUtils.isNotBlank(nlzmc)){
			conditionSb.append(" and nlzmc like  ? ");
			conditionValue.add("%" + nlzmc + "%");
		}
		
		String conditionNullReturnNull = parameters.getString("conditionNullReturnNull");
		boolean flag = StringUtils.isBlank(conditionNullReturnNull) ? false : Boolean.valueOf(conditionNullReturnNull);
		if(flag && !conditionSb.toString().contains("and")){
			bundle.put("nlzList", new ArrayList<Map<String,Object>>());
			bundle.put("nlz", new HashMap<String,Object>());
			return;
		}
		
		Dataset dataset = Sys.query(TableConstant.能力组, "nlzid,nlzbh,nlzmc,nlzms,zzjgid", conditionSb.toString(), null, null,conditionValue.toArray());
		bundle.put("nlzList", dataset.getList());
		bundle.put("nlz", dataset.getMap());
 	}
	
	/**
	 * 根据条件 查询设备日历信息表
	 * @param parameters
	 * @param bundle
	 */
	public void query_sbgs(Parameters parameters, Bundle bundle) {
		StringBuffer condition = new StringBuffer(" 1 = 1 ");
		List<Object> conditionValues = new ArrayList<Object>();
		String sbid = parameters.getString("sbid");
		if(StringUtils.isNotBlank(sbid)){
			condition.append(" and sbid = ? ");
			conditionValues.add(sbid);
		}
		
		Date rq = parameters.getDate("rq");
		if(rq != null){
			condition.append(" and ksrq <= ? and jsrq >= ? ");
			conditionValues.add(rq);
			conditionValues.add(rq);
		}
		Dataset dataset = Sys.query(TableConstant.设备工时表, "sbgsid,sbid,gsmbid,ksrq,jsrq", condition.toString(), 
				null, conditionValues.toArray());
		bundle.put("sbgs", dataset.getMap());
		bundle.put("sbgsList", dataset.getList());
	}
	
	public void query_sbrl(Parameters parameters, Bundle bundle){
		StringBuffer sb_condition = new StringBuffer(" 1 = 1 ");
		List<Object> valueList = new ArrayList<Object>();
		String sbid = parameters.getString("sbid");
		if(StringUtils.isNotBlank(sbid)){
			sb_condition.append(" and sbid = ? ");
			valueList.add(sbid);
		}
		String start = parameters.getString("start");
		if(StringUtils.isNotBlank(start)){
			sb_condition.append(" and rlrq >= ? ");
			valueList.add(DateTimeUtils.string2Date(start));
		}
		String end = parameters.getString("end");
		if(StringUtils.isNotBlank(end)){
			sb_condition.append(" and rlrq <= ? ");
			valueList.add(DateTimeUtils.string2Date(end));
		}
		Dataset dataset = Sys.query("em_sbrlb", "rlzt,rlrq,rlms", 
				sb_condition.toString(), null,valueList.toArray());
		bundle.put("sbrl", dataset.getMap());
		bundle.put("sbrlList", dataset.getList());
	}
	
	/**
	 * 根据能力组id查询设备信息
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	public void query_sbxxByNlzid(Parameters parameters,Bundle bundle){
		String nlzid = parameters.getString("nlzid");
		if(StringUtils.isBlank(nlzid)){
			bundle.put("list", null);
			return;
		}
		//关联表的条件
		String[] models = new String[]{TableConstant.设备信息表,TableConstant.能力组设备关联表};
		String join = new StringBuffer()
				.append(TableConstant.能力组设备关联表).append(SqlConstant.JOIN)
				.append(TableConstant.设备信息表).append(SqlConstant.ON)
				.append(TableConstant.能力组设备关联表).append(SqlConstant.PERIOD).append("sbid")
				.append(SqlConstant.EQUALS)
				.append(TableConstant.设备信息表).append(SqlConstant.PERIOD).append("sbid").toString();
		StringBuffer fieldsSb = new StringBuffer()
				.append(TableConstant.能力组设备关联表).append(SqlConstant.PERIOD)
				.append("nlzid").append(SqlConstant.COMMA)//设备id
				.append(TableConstant.设备信息表).append(SqlConstant.PERIOD)
				.append("sbid").append(SqlConstant.COMMA)//设备id
				.append(TableConstant.设备信息表).append(SqlConstant.PERIOD)
				.append("sbmc").append(SqlConstant.COMMA)//设备名称 
				.append(TableConstant.设备信息表).append(SqlConstant.PERIOD)
				.append("sbbh").append(SqlConstant.COMMA)//设备编号
				.append(TableConstant.设备信息表).append(SqlConstant.PERIOD)
				.append("zzjgid").append(SqlConstant.COMMA);//组织机构ID
		String fields = fieldsSb.deleteCharAt(fieldsSb.length() - 1).toString();
		Dataset data = Sys.query(models, join, fields, " nlzid = ? ", null ,null, new Object[]{nlzid});
		List<Map<String,Object>> sbList = data.getList();
		bundle.put("list", sbList);
	}
	
	
	@SuppressWarnings("unchecked")
	public String pic_display(Parameters parameters, Bundle bundle) {
		Bundle filebundle = Sys.callModuleService("pm", "fileInfoService", parameters);
		if (null!=filebundle) {
			List<Map<String, Object>> sbxxList = (List<Map<String, Object>>) filebundle.get("fileInfo");
			if (null != sbxxList && sbxxList.size() == 1) {
				String wjmc = (String) sbxxList.get(0).get("wjmc");
				String wjlj = (String) sbxxList.get(0).get("wjlj");
				int wjdx = Integer.parseInt(sbxxList.get(0).get("wjdx").toString());
				String wjlb = (String) sbxxList.get(0).get("wjlb");
				File file_display = null;
				try{
					if(Sys.existFile(wjlj))
					{
						 file_display = new File(wjmc, null, Sys.readFile(wjlj), wjlb, wjdx);
					}
					 bundle.put("file_display", file_display);
				} catch (Exception e){
					e.printStackTrace();
				} 
			} else {
				bundle.put("file_display", null);
			}
		}
		return "file:file_display";
	}

	public void query_sbxxFile(Parameters parameters, Bundle bundle) {
		String sbbh = parameters.getString("sbbh");
		Dataset dataset_sbxx = Sys.query(TableConstant.设备信息表,"sbid,sbbh,sbmc,sblxid,ccbh,ip,cj,zzjgid,jyrq,sbwz,kfid,sbtpid", "sbbh = ? ", null,new Object[]{sbbh});
		Map<String, Object> map_sb  = new HashMap<String, Object>();
		if(dataset_sbxx.getList().size()>0){
			map_sb = dataset_sbxx.getList().get(0);
			map_sb.put("url", Sys.getAbsoluteUrl("/service/pic_display?wjid="+map_sb.get("sbtpid")));
			
			int sblxid = Integer.parseInt(map_sb.get("sblxid").toString());
			Dataset dataset_sblxxx = Sys.query("em_sblxb","sblxmc", "sblxid = ? ", null,new Object[]{sblxid});
			if(dataset_sblxxx.getList().size()>0){
				map_sb.put("sblxmc", dataset_sblxxx.getList().get(0).get("sblxmc"));
			} else {
				map_sb.put("sblxmc", "");
			}
		}
		bundle.put("sbxx", map_sb);
	}
	
	public boolean paramCheck(Parameters parameters, Bundle bundle){
		boolean flag = false;
		if(StringUtils.isBlank(parameters.getString("sbid"))){
			bundle.setError("设备id不能为空");
			return flag;
		}
		
		if(parameters.get("begin") == null){
			bundle.setError("开始时间不能为空");
			return flag;
		}
		
		if(parameters.get("end") == null){
			bundle.setError("结束时间不能为空");
			return flag;
		}
		Date begin = null;
		try{
			begin = (Date) parameters.get("begin");
		}catch(Exception e){
			bundle.setError("开始时间参数不合法");
			return flag;
		}
		
		Date end = null;
		try{
			end = (Date) parameters.get("end");
		}catch(Exception e){
			bundle.setError("结束时间参数不合法");
			return flag;
		}
		
		if(begin.compareTo(end) > 0){
			bundle.setError("开始时间比结束时间大");
			return flag;
		}
		flag = true;
		return flag;
	}
	
	/**
	 * 得到设备 日历
	 * @param sbid
	 * @param start
	 * @param end
	 * @return
	 */
	public Map<String,Object> getSbCalendarDays(String sbid ,String start,String end){
		Map<String,Object> returnMap = new HashMap<String, Object>();
		
		List<String> workdayList = new ArrayList<String>();
		List<String> restdayList = new ArrayList<String>();
		
		Map<String,Map<String,Object>> restdayObjectMap = new HashMap<String, Map<String,Object>>();
		
		Dataset dataset = Sys.query("em_sbrlb", "rlzt,rlrq,rlms", 
				" sbid = ? and  rlrq >= ? and rlrq <= ? ", null, 
				new Object[]{sbid,DateTimeUtils.string2Date(start),DateTimeUtils.string2Date(end)});
		List<Map<String,Object>> specialDays = dataset.getList();
			if(CollectionUtils.isNotEmpty(specialDays)){
				for(Map<String,Object> m  : specialDays){
					String rlztdm = m.get("rlzt").toString();
					Date rlrq =  (Date) m.get("rlrq");
					String rlrq_str = DateTimeUtils.date2String(rlrq, "yyyy-MM-dd");
					//工作日
					if("10".equals(rlztdm)){
						workdayList.add(rlrq_str);
					}
					//休息日
					if("20".equals(rlztdm)){
						restdayList.add(rlrq_str);
						String reason = "假期安排  " + m.get("rlms");
						m.put("reason", reason);
						restdayObjectMap.put(rlrq_str, m);
					}
					//休息日
					if("30".equals(rlztdm)){
						restdayList.add(rlrq_str);
						String reason = "维修安排  " + m.get("rlms");
						m.put("reason", reason);
						restdayObjectMap.put(rlrq_str, m);
					}
					//休息日
					if("40".equals(rlztdm) ){
						restdayList.add(rlrq_str);
						String reason = "点检安排  " + m.get("rlms");
						m.put("reason", reason);
						restdayObjectMap.put(rlrq_str, m);
					}
					//休息日
					if("50".equals(rlztdm)){
						restdayList.add(rlrq_str);
						String reason = "保养安排  " + m.get("rlms");
						m.put("reason", reason);
						restdayObjectMap.put(rlrq_str, m);
					}
				}
			}
		
		returnMap.put("workdayList", workdayList);
		returnMap.put("restdayList", restdayList);
		returnMap.put("restdayObjectMap", restdayObjectMap);
		return returnMap;
	}
	
	/**
	 * 得到一个设备  在某段时间内   不工作的时间段和原因
	 * for wangxu  工单排产
	 * 逻辑说明：
	 * 	1.判断在设备日历表中   某天是否存在特殊数据，如果存在，该天以设备日历为准
	 * 	2.如果没有特殊数据  去查看工厂日历 查看这天的休息/工作的状态
	 *  3.去查看这天的工时模版
	 * @param parameters
	 * @param bundle
	 * return List<Map<String,Object>
	 * map key -> begin ： 开始时间
	 * 			  end : 结束时间
	 * 			  reason : 不工作的时间原因
	 */
	public void query_sb_cannotJobTime(Parameters parameters, Bundle bundle){
		if(!paramCheck(parameters, bundle)){
			return ;
		}
		
		String sbid = parameters.getString("sbid");
		Date begin = (Date) parameters.get("begin");
		String beginStr_date =  DateTimeUtils.date2String(begin, "yyyy-MM-dd");
		String beginStr_dateTime =   DateTimeUtils.date2String(begin, "yyyy-MM-dd HH:mm:ss");
		Calendar startCal = Calendar.getInstance(); 
		startCal.setTime(begin);
		
		
		Date end = (Date) parameters.get("end");
		String endStr_date =  DateTimeUtils.date2String(end, "yyyy-MM-dd");
		String endStr_dateTime =   DateTimeUtils.date2String(begin, "yyyy-MM-dd HH:mm:ss");
		Calendar endCal = Calendar.getInstance(); 
		endCal.setTime(end);
		
		//设备日历
		Map<String,Object> sb_calendar_map =  getSbCalendarDays(sbid, beginStr_date, endStr_date);
		List<String> sb_workdayList = (List<String>) sb_calendar_map.get("workdayList");
		List<String> sb_restdayList = (List<String>) sb_calendar_map.get("restdayList");
		Map<String,Map<String,Object>> restdayObjectMap 
				=  (Map<String,Map<String,Object>>) sb_calendar_map.get("restdayObjectMap");
		//工厂日历
		parameters.set("start", beginStr_date);
		parameters.set("end", endStr_date);
		Bundle b_fm = Sys.callModuleService("fm", "fmService_getGcCalendarDays", parameters);
		List<String> weekendsList = (List<String>) b_fm.get("weekendsList");
		Map<String,Map<String,Object>> restDayMap = new HashMap<String, Map<String,Object>>();
		
		Map<String,Object> gcrlmb = (Map<String, Object>) b_fm.get("gcrlmb");
		Date thisTime = null;
		String thisDateStr = "";
		String thisDateTimeStr = "";
		
		List<Map<String,Object>> returnList = new ArrayList<Map<String,Object>>();
		List<Map<String,Object>> allDayList = new ArrayList<Map<String,Object>>();
 		while(startCal.getTime().compareTo(end) < 1){
			
			thisTime = startCal.getTime();
			thisDateStr = DateTimeUtils.date2String(thisTime, "yyyy-MM-dd");
			thisDateTimeStr = DateTimeUtils.date2String(thisTime, "yyyy-MM-dd HH:mm:ss");
			
			Date oneDayBeginTime = DateTimeUtils.getOneDayBeginTime(thisTime);
			Date oneDayEndTime = DateTimeUtils.getOneDayEndTime(thisTime);
			
			//如果 设备日历休息
			if(sb_restdayList.contains(thisDateStr)){
				
				Map<String,Object> map = new HashMap<String, Object>();
				map.put("date", oneDayBeginTime);
				map.put("begin", oneDayBeginTime);
				map.put("end", oneDayEndTime);
				map.put("reason", (String) restdayObjectMap.get(thisDateStr).get("reason"));
				map.put("source", "device");
				allDayList.add(map);
				
				startCal.add(Calendar.DAY_OF_MONTH, 1);
				continue;
			}
			
			
			//如果 设备日历正常   需要去计算工时
			else if(sb_workdayList.contains(thisDateStr)){
				Map<String,Object> map = getMbxx(sbid, thisTime, parameters);
				List<Map<String,Object>> list = getOneDayNoJob(map, startCal, oneDayBeginTime, oneDayEndTime);
				returnList.addAll(list);
				
				startCal.add(Calendar.DAY_OF_MONTH, 1);
				continue;
			}
			
			//如果设备日历里没有设置    那么查看工厂日历
			//在工厂日历里是不是休息的  如果是休息的   那个这个设备也是休息的
			else if(weekendsList.contains(thisDateStr)){
				
				Map<String,Object> map = new HashMap<String, Object>();
				map.put("date", oneDayBeginTime);
				map.put("begin", oneDayBeginTime);
				map.put("end", oneDayEndTime);
				String reason = "";
				if(restDayMap.containsKey(thisDateStr)){
					reason = (String) restDayMap.get("thisDateStr").get("rlms");
				}else{
					reason = "周末休息";
				}
				map.put("reason",reason);
				allDayList.add(map);
				map.put("source", "factory");
				
				startCal.add(Calendar.DAY_OF_MONTH, 1);
				continue;
			}
			
			//如果设备日历里没有设置    工厂日历里是上班，则先去查看设备对应的模版，如果没有  使用默认模版
			else{
				
				Map<String,Object> map = getMbxx(sbid, thisTime, parameters);
				List<Map<String,Object>> list = getOneDayNoJob(map, startCal, oneDayBeginTime, oneDayEndTime);
				returnList.addAll(list);
				
				startCal.add(Calendar.DAY_OF_MONTH, 1);
			}
			
		}
		bundle.put("list", returnList);
		
		bundle.put("allDaylist", allDayList);
	}
	
	/**
	 * 查询一段时间内  设备可以工作的时间
	 * @param parameters
	 * @param bundle
	 */
	public void query_sb_shouldJobTime(Parameters parameters, Bundle bundle){
		if(!paramCheck(parameters, bundle)){
			return ;
		}
		
		String sbid = parameters.getString("sbid");
		Date begin = (Date) parameters.get("begin");
		String beginStr_date =  DateTimeUtils.date2String(begin, "yyyy-MM-dd");
		String beginStr_dateTime =   DateTimeUtils.date2String(begin, "yyyy-MM-dd HH:mm:ss");
		Calendar startCal = Calendar.getInstance(); 
		startCal.setTime(begin);
		
		Date end = (Date) parameters.get("end");
		String endStr_date =  DateTimeUtils.date2String(end, "yyyy-MM-dd");
		String endStr_dateTime =   DateTimeUtils.date2String(begin, "yyyy-MM-dd HH:mm:ss");
		Calendar endCal = Calendar.getInstance(); 
		endCal.setTime(end);
		
		//设备日历
		Map<String,Object> sb_calendar_map =  getSbCalendarDays(sbid, beginStr_date, endStr_date);
		List<String> sb_workdayList = (List<String>) sb_calendar_map.get("workdayList");
		List<String> sb_restdayList = (List<String>) sb_calendar_map.get("restdayList");
		Map<String,Map<String,Object>> restdayObjectMap 
				=  (Map<String,Map<String,Object>>) sb_calendar_map.get("restdayObjectMap");
		//工厂日历
		parameters.set("start", beginStr_date);
		parameters.set("end", endStr_date);
		Bundle b_fm = Sys.callModuleService("fm", "fmService_getGcCalendarDays", parameters);
		List<String> weekendsList = (List<String>) b_fm.get("weekendsList");
		Map<String,Map<String,Object>> restDayMap = new HashMap<String, Map<String,Object>>();
		
		Map<String,Object> gcrlmb = (Map<String, Object>) b_fm.get("gcrlmb");
		Date thisTime = null;
		String thisDateStr = "";
		String thisDateTimeStr = "";
		
		List<Map<String,Object>> returnList = new ArrayList<Map<String,Object>>();
 		while(startCal.getTime().compareTo(end) < 1){
			
			thisTime = startCal.getTime();
			thisDateStr = DateTimeUtils.date2String(thisTime, "yyyy-MM-dd");
			thisDateTimeStr = DateTimeUtils.date2String(thisTime, "yyyy-MM-dd HH:mm:ss");
			
			Date oneDayBeginTime = DateTimeUtils.getOneDayBeginTime(thisTime);
			Date oneDayEndTime = DateTimeUtils.getOneDayEndTime(thisTime);
			
			//如果设备日历上班，需要纳入应该工作的工时
			if(sb_workdayList.contains(thisDateStr)){
				Map<String,Object> map = getMbxx(sbid, thisTime, parameters);
				Map<String,Object> returnValueMap = getOneDayJob(map, startCal);
				returnList.add(returnValueMap);
				
				startCal.add(Calendar.DAY_OF_MONTH, 1);
				continue;
			}
			
			
			//如果 设备日历不上班  不用纳入
			else if(sb_workdayList.contains(thisDateStr)){
				startCal.add(Calendar.DAY_OF_MONTH, 1);
				continue;
			}
			
			//如果设备日历里没有设置    那么查看工厂日历
			//在工厂日历里是不是休息的  如果是休息的   那个这个设备也是休息的，不纳入
			else if(weekendsList.contains(thisDateStr)){
				startCal.add(Calendar.DAY_OF_MONTH, 1);
				continue;
			}
			
			//如果设备日历里没有设置    工厂日历里是上班，则先去查看设备对应的模版，如果没有  使用默认模版
			else{
				Map<String,Object> map = getMbxx(sbid, thisTime, parameters);
				Map<String,Object> returnValueMap = getOneDayJob(map, startCal);
				returnList.add(returnValueMap);
				startCal.add(Calendar.DAY_OF_MONTH, 1);
			}
			
		}
		bundle.put("list", returnList);
	}
	
	
	private Map<String,Object> getMbxx(String sbid,Date thisTime,Parameters parameters){
		//去查看 设备这段时间使用的模版
		Dataset dataset = Sys.query("em_sbgsb", "sbgsid,sbid,gsmbid,ksrq,jsrq",
				" sbid = ? and ksrq <= ? and jsrq >= ?", 
				null, new Object[]{sbid,thisTime,thisTime});
		//如果 模版没有配置，使用默认模版
		if(dataset.getCount() == 0){
			parameters.set("gsmbid", null);
			parameters.set("sfmrmb", "10");
		}
		else{
			String gsmbid = dataset.getMap().get("gsmbid").toString();
			parameters.set("gsmbid", gsmbid);
			parameters.set("sfmrmb", null);
		}
		Bundle b_fm_gs = Sys.callModuleService("fm", "fmService_query_gcgsmbb", parameters);
		Map<String,Object> map = (Map<String, Object>) b_fm_gs.get("gcgsmbb");
		return map;
	}
	
	private Map<String,Object> getOneDayJob(Map<String,Object> map ,Calendar calendar){
		int weekday = calendar.get(Calendar.DAY_OF_WEEK);
		Date workBegin = null;
		Date workEnd = null;
		if(weekday == Calendar.MONDAY){
			workBegin = (Date) map.get("zhouyiks");
			workEnd = (Date) map.get("zhouyijs");
		}
		if(weekday == Calendar.TUESDAY){
			workBegin = (Date) map.get("zhouerks");
			workEnd = (Date) map.get("zhouerjs");
		}
		if(weekday == Calendar.WEDNESDAY){
			workBegin = (Date) map.get("zhousanks");
			workEnd = (Date) map.get("zhousanjs");
		}
		if(weekday == Calendar.THURSDAY){
			workBegin = (Date) map.get("zhousiks");
			workEnd = (Date) map.get("zhousijs");
		}
		if(weekday == Calendar.FRIDAY){
			workBegin = (Date) map.get("zhouwuks");
			workEnd = (Date) map.get("zhouwujs");
		}
		if(weekday == Calendar.SATURDAY){
			workBegin = (Date) map.get("zhouliuks");
			workEnd = (Date) map.get("zhouliujs");
		}
		if(weekday == Calendar.SUNDAY){
			workBegin = (Date) map.get("zhouriks");
			workEnd = (Date) map.get("zhourijs");
		}
		
		String date = DateTimeUtils.date2String(calendar.getTime(), "yyyy-MM-dd");
		String begin = DateTimeUtils.date2String(workBegin, "HH:mm:ss");
		String end = DateTimeUtils.date2String(workEnd, "HH:mm:ss");
		
		workBegin = DateTimeUtils.string2Date(date + " " + begin );
		workEnd = DateTimeUtils.string2Date(date + " " + end );
		
		Map<String,Object> returnMap = new HashMap<String, Object>();
		returnMap.put("begin", workBegin);
		returnMap.put("end", workEnd);
		BigDecimal work_seconds = new BigDecimal(workEnd.getTime() - workBegin.getTime()).divide(new BigDecimal(1000));
		returnMap.put("work_seconds", work_seconds);
		BigDecimal work_hours = work_seconds.divide(new BigDecimal(3600), 2, BigDecimal.ROUND_HALF_UP);
		returnMap.put("work_hours", work_hours);
		
		return returnMap;
	
	}
	
	/**
	 * 
	 * @param map  日历模版
	 * @param date
	 * @return
	 */
	private List<Map<String,Object>> getOneDayNoJob(Map<String,Object> map ,Calendar calendar,
			Date oneDayBeginTime,Date oneDayEndTime){
		List<Map<String,Object>> returnList = new ArrayList<Map<String,Object>>();
		int weekday = calendar.get(Calendar.DAY_OF_WEEK);
		Date workBegin = null;
		Date workEnd = null;
		if(weekday == Calendar.MONDAY){
			workBegin = (Date) map.get("zhouyiks");
			workEnd = (Date) map.get("zhouyijs");
		}
		if(weekday == Calendar.TUESDAY){
			workBegin = (Date) map.get("zhouerks");
			workEnd = (Date) map.get("zhouerjs");
		}
		if(weekday == Calendar.WEDNESDAY){
			workBegin = (Date) map.get("zhousanks");
			workEnd = (Date) map.get("zhousanjs");
		}
		if(weekday == Calendar.THURSDAY){
			workBegin = (Date) map.get("zhousiks");
			workEnd = (Date) map.get("zhousijs");
		}
		if(weekday == Calendar.FRIDAY){
			workBegin = (Date) map.get("zhouwuks");
			workEnd = (Date) map.get("zhouwujs");
		}
		if(weekday == Calendar.SATURDAY){
			workBegin = (Date) map.get("zhouliuks");
			workEnd = (Date) map.get("zhouliujs");
		}
		if(weekday == Calendar.SUNDAY){
			workBegin = (Date) map.get("zhouriks");
			workEnd = (Date) map.get("zhourijs");
		}
		
		String date = DateTimeUtils.date2String(calendar.getTime(), "yyyy-MM-dd");
		String begin = DateTimeUtils.date2String(workBegin, "HH:mm:ss");
		String end = DateTimeUtils.date2String(workEnd, "HH:mm:ss");
		
		workBegin = DateTimeUtils.string2Date(date + " " + begin );
		workEnd = DateTimeUtils.string2Date(date + " " + end );
		
		Map<String,Object> map1 = new HashMap<String, Object>();
		map1.put("begin", oneDayBeginTime);
		map1.put("end", workBegin);
		map1.put("reason", "正常休息");
		returnList.add(map1);
		
		
		Map<String,Object> map2 = new HashMap<String, Object>();
		map2.put("begin", workEnd);
		map2.put("end", oneDayEndTime);
		map2.put("reason", "正常休息");
		returnList.add(map2);
		
		return returnList;
	}
	
	
	public void query_sbxxListBybh(Parameters parameters, Bundle bundle) {
		String sbbh = parameters.getString("sbbh");
		Dataset dataset_sbxx = Sys.query(TableConstant.设备信息表,"sbid,sbbh,sbmc,sblxid,ccbh,ip,cj,zzjgid,jyrq,sbwz,kfid,sbtpid", "sbbh = ? ", null,new Object[]{sbbh});
		bundle.put("sbxxList", dataset_sbxx.getList());
		bundle.put("sbxx", dataset_sbxx.getMap());
	}
	
	/**
	 * 设备类型查询
	 * @param parameters
	 * @param bundle
	 */
	public void query_sblx_by_param(Parameters parameters, Bundle bundle){
		StringBuffer conditionSb = new StringBuffer(" 1 = 1 ");
		List<Object> conditionValue = new ArrayList<Object>();
		//设备类型id
		List<Map<String,Object>> sblxidList = (List<Map<String, Object>>) parameters.get("sblxids");
		if(CollectionUtils.isNotEmpty(sblxidList)){
			StringBuffer sb = new StringBuffer("'");
			for(Map<String,Object> map : sblxidList){
				if(map.get("sblxid") == null || StringUtils.isBlank( map.get("sblxid").toString())){
					continue;
				}
				sb = sb.append(map.get("sblxid").toString()).append("','");
			}
			if(sb.toString().endsWith(",'")){
				sb = sb.delete(sb.length() - 2, sb.length());
			}
			conditionSb.append(" and sblxid in (").append(sb).append(")");
		}
		Map<String,Object> idMap = (Map<String, Object>) parameters.get("sblxid");
		if(MapUtils.isNotEmpty(idMap)){
			String sblxid = idMap.get("sblxid").toString();
			conditionSb.append(" and sblxid = ? ");
			conditionValue.add(sblxid);
		}
		
		String sblxid =  parameters.getString("sblxidStr");
		if(StringUtils.isNotBlank(sblxid)){
			conditionSb.append(" and sblxid =  ? ");
			conditionValue.add(sblxid );
		}
		
		//设备类型编号
		String sblxbh =  parameters.getString("sblxbh");
		if(StringUtils.isNotBlank(sblxbh)){
			conditionSb.append(" and sblxbh like  ? ");
			conditionValue.add("%" + sblxbh + "%");
		}
		String sblxbh_eq =  parameters.getString("sblxbh_eq");
		if(StringUtils.isNotBlank(sblxbh_eq)){
			conditionSb.append(" and sblxbh =  ? ");
			conditionValue.add(sblxbh_eq);
		}
		
		//设备类型名称
		String sblxmc =  parameters.getString("sblxmc");
		if(StringUtils.isNotBlank(sblxmc)){
			conditionSb.append(" and nlzmc like  ? ");
			conditionValue.add("%" + sblxmc + "%");
		}
		
		//设备分类代码
		String sbfldm =  parameters.getString("sbfldm");
		if(StringUtils.isNotBlank(sbfldm)){
			conditionSb.append(" and sbfldm =  ? ");
			conditionValue.add(sbfldm);
		}
		
		//启动标志
		String qybsdm =  parameters.getString("qybsdm");
		if(StringUtils.isNotBlank(qybsdm)){
			conditionSb.append(" and qybsdm =  ? ");
			conditionValue.add(Integer.valueOf(qybsdm));
		}
		
		String conditionNullReturnNull = parameters.getString("conditionNullReturnNull");
		boolean flag = StringUtils.isBlank(conditionNullReturnNull) ? false : Boolean.valueOf(conditionNullReturnNull);
		if(flag && !conditionSb.toString().contains("and")){
			bundle.put("sblxList", new ArrayList<Map<String,Object>>());
			bundle.put("sblx", new HashMap<String,Object>());
			return;
		}
		
		Dataset dataset = Sys.query(TableConstant.设备类型表, 
				"sblxid,sblxbh,sblxmc,xhph,skxtdm,nlms,qybsdm,sbfldm",
				 conditionSb.toString(), null, null,conditionValue.toArray());
		
		bundle.put("sblxList", dataset.getList());
		bundle.put("sblx", dataset.getMap());
 	
	}
	
	/**
	 * 根据设备类型IDs和加工单元编号查询加工单元
	 * 
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	public void query_jgdyBySblxids(Parameters parameters, Bundle bundle) {
		// 关联表的条件
		String[] models = new String[] { TableConstant.加工单元, TableConstant.加工单元设备关联表, TableConstant.设备信息表 };
		String join = new StringBuffer()
				.append(TableConstant.加工单元).append(SqlConstant.JOIN)
				.append(TableConstant.加工单元设备关联表).append(SqlConstant.ON)
				.append(TableConstant.加工单元设备关联表).append(SqlConstant.PERIOD).append("jgdyid")
				.append(SqlConstant.EQUALS).append(TableConstant.加工单元).append(SqlConstant.PERIOD).append("jgdyid")
				.append(SqlConstant.JOIN)
				.append(TableConstant.设备信息表).append(SqlConstant.ON).append(TableConstant.加工单元设备关联表)
				.append(SqlConstant.PERIOD).append("sbid").append(SqlConstant.EQUALS).append(TableConstant.设备信息表)
				.append(SqlConstant.PERIOD).append("sbid").toString();
		String fields = TableConstant.加工单元 + ".jgdyid, jgdybh, jgdymc";
		String groupby = null;
		String orderby = "jgdybh asc";
		String condition = "1=1";
		
		List<Object> conditionValue = new ArrayList<Object>();

		// 设备类型条件
		String sblxids = parameters.getString("sblxids");
		if (StringUtils.isNotBlank(sblxids)) {
			condition += " and " + TableConstant.设备信息表 + SqlConstant.PERIOD + "sblxid in(" + sblxids + ")";
		}
		// 设备类型条件
		String jgdybh = parameters.getString("jgdybh");
		if (StringUtils.isNotBlank(jgdybh)) {
			condition += " and " + TableConstant.加工单元 + SqlConstant.PERIOD + "jgdybh like '%" + jgdybh + "%'";
		}
		
		String sbid = parameters.getString("sbid");
		if (StringUtils.isNotBlank(sbid)) {
			condition += " and " + TableConstant.设备信息表 + SqlConstant.PERIOD + "sbid = ? ";
			conditionValue.add(sbid);
		}
		
		String groupBy = null;
		String orderBy = null;
		Dataset dataset = Sys.query(models, join, fields, condition, groupby, orderby, conditionValue.toArray());
		List<Object> addedJgdyIds = Lists.newArrayList();
		List<Map<String, Object>> rows = Lists.newArrayList();
		for (Map<String, Object> row : dataset.getList()) {
			if (row.get("jgdyid") == null) {
				bundle.put("error", "空的加工单元ID");
				return;
			}
			if (addedJgdyIds.contains(row.get("jgdyid"))) {
				continue;
			}
			addedJgdyIds.add(row.get("jgdyid"));
			rows.add(row);
		}
		bundle.put("data", rows);
	}
	
	/**
	 * 根据加工单元ID查询加工单元信息
	 * 
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	public void query_jgdyById(Parameters parameters, Bundle bundle) {
		String jgdyid = parameters.getString("jgdyid");
		Map<String, Object> data = Sys.query("em_jgdyb", "jgdybh,jgdymc,jgdyms,bdgkjbh,zzjgid", "jgdyid=?", null, null, jgdyid).getMap();
		bundle.put("data", data);
	}
	
	public void query_jgdyByIds(Parameters parameters, Bundle bundle) {
		List<String> jgdyids = (List<String>) parameters.get("jgdyids");
		int page = parameters.getInteger("page") == null ? 1 : parameters.getInteger("page");
		int pageSize = parameters.getInteger("pageSize") == null ? 10 : parameters.getInteger("pageSize");
		StringBuffer condition = new StringBuffer();
		if(CollectionUtils.isNotEmpty(jgdyids)){
			condition.append("jgdyid in (");
			for(String id : jgdyids){
				condition.append("'").append(id).append("',");
			}
			condition.deleteCharAt(condition.length() -1).append(")");
		}else{
			condition.append(" 1 = 0 ") ;
		}
		Dataset dataset = Sys.query("em_jgdyb", "jgdyid,jgdybh,jgdymc,jgdyms,bdgkjbh,zzjgid", 
				condition.toString(), null,"jgdybh",(page-1)*pageSize, pageSize, new Object[]{});
		
		bundle.put("jgdy", dataset.getMap());
		bundle.put("jgdyList", dataset.getList());
		
		int totalPage = dataset.getTotal()%pageSize==0?dataset.getTotal()/pageSize:dataset.getTotal()/pageSize+1;
		bundle.put("totalPage", totalPage);
		bundle.put("currentPage", page);
		bundle.put("totalRecord", dataset.getTotal());
	}
	
	public void query_jgdyByGkjIP(Parameters parameters, Bundle bundle) {
		String gkjIP = parameters.getString("gkjIP");
		Dataset dataset  = Sys.query("em_jgdyb", "jgdyid,jgdybh,jgdymc,jgdyms,bdgkjbh", "bdgkjbh=?", null, null, new Object[]{gkjIP});
		bundle.put("data", dataset.getList());
	}
	
	public void query_jgdyByParam(Parameters parameters, Bundle bundle) {
		StringBuffer conditionSb = new StringBuffer(" 1 = 1 ");
		List<Object> conditionValue = new ArrayList<Object>();
		
		String jgdyid = parameters.getString("jgdyid");
		if(StringUtils.isNotBlank(jgdyid)){
			conditionSb = conditionSb.append(" and jgdyid = ? ") ;
			conditionValue.add(jgdyid);
		}
		
		String jgdymc = parameters.getString("jgdymc");
		if(StringUtils.isNotBlank(jgdymc)){
			conditionSb = conditionSb.append(" and jgdymc like ? ") ;
			conditionValue.add("%"+jgdymc+"%");
		}
		
		String jgdybh = parameters.getString("jgdybh");
		if(StringUtils.isNotBlank(jgdybh)){
			conditionSb = conditionSb.append(" and jgdybh like ? ") ;
			conditionValue.add("%"+jgdybh+"%");
		}
		
		Dataset dataset = Sys.query("em_jgdyb", "jgdyid,jgdybh,jgdymc,jgdyms,bdgkjbh",
				conditionSb.toString(), null, null, conditionValue.toArray());
		bundle.put("data", dataset.getMap());
		bundle.put("dataList", dataset.getList());
	}
	
	/**
	 * 设备刀位计数
	 * @param parameters
	 * @param bundle
	 */
	public void sbdwjs(Parameters parameters, Bundle bundle){
		String sbid = parameters.getString("sbid");
		String dw = parameters.getString("dw");
		Dataset dataset = Sys.query(TableConstant.设备刀位表, 
				"sbdwid,dpjgsl,ljjgsl", " sbid = ? and dw = ? ", null, new Object[]{sbid,dw});
		if(MapUtils.isEmpty(dataset.getMap())){
			Map<String,Object> insertMap = new HashMap<String, Object>();
			insertMap.put("sbid", sbid);
			insertMap.put("dw", dw);
			insertMap.put("dpjgsl", 1);
			insertMap.put("ljjgsl", 1);
			Sys.insert(TableConstant.设备刀位表, insertMap);
		}else{
			Map<String,Object> queryMap = dataset.getMap();
			Map<String,Object> updateMap = new HashMap<String, Object>();
			updateMap.put("dpjgsl", queryMap.get("dpjgsl"));
			updateMap.put("ljjgsl", queryMap.get("ljjgsl"));
			Sys.update(TableConstant.设备刀位表, updateMap, " sbdwid = ? ", 
					new Object[]{queryMap.get("sbdwid").toString()});
		}
	}
	
	/**
	 * 设备的刀位清零
	 * @param parameters
	 * @param bundle
	 */
	public void sbdwql(Parameters parameters, Bundle bundle){
		String sbid = parameters.getString("sbid");
		String dw = parameters.getString("dw");
		Map<String,Object> updateMap = new HashMap<String, Object>();
		updateMap.put("dpjgsl", 0);
		int count = Sys.update(TableConstant.设备刀位表, updateMap, " sbid = ? and dw = ? ", new Object[]{sbid,dw});
		bundle.put("count", count);
	}
	/**
	 * 通过设备编号查询设备信息
	 * @param parameters
	 * @param bundle
	 */
	public void query_sbxx_by_sbbh(Parameters parameters, Bundle bundle) {
		String param = " 1=1 ";
		if(null!=parameters.getString("val_sb")&&!"".equals(parameters.getString("val_sb"))){
			param+=" and sbbh in "+parameters.getString("val_sb");
		}
		if(null!=parameters.getString("sbwz")&&!"".equals(parameters.getString("sbwz"))){
			param+=" and sbwz like '%"+parameters.getString("sbwz")+"%'";
		}
		Dataset dataset_sbxx = Sys.query(TableConstant.设备信息表,"sbid,sbbh,sbmc,sblxid,ccbh,ip,cj,zzjgid,jyrq,sbwz,kfid,sbtpid", param , null,new Object[]{});
		bundle.put("sbxx", dataset_sbxx.getList());
	}
	
	/**
	 * 查询所有设备
	 * @param parameters
	 * @param bundle
	 */
	public void query_all_sb(Parameters parameters, Bundle bundle){
		Dataset dataset_sbxx = Sys.query(TableConstant.设备信息表, "sbid,sbmc,sblxid,ccbh,ip,cj,zzjgid,jyrq,sbwz,sbbh,sbtpid",
				"1=1", null, new Object[]{});
		
		bundle.put("sbxxlist", dataset_sbxx.getList());
	}
	
	public void query_djsm(Parameters parameters, Bundle bundle) {
		String jgdyid = parameters.getString("jgdyid");
		Dataset dataset = Sys.query(new String[] {"em_jgdyb", "em_jgdysbglb", "em_sbxxb" ,"em_sbdwb"},
				"em_sbdwb left join em_sbxxb on em_sbxxb.sbid = em_sbdwb.sbid left join em_jgdysbglb on em_jgdysbglb.sbid = em_sbxxb.sbid left join em_jgdyb on em_jgdyb.jgdyid = em_jgdysbglb.jgdyid",
				"sbbh,sbmc,em_sbxxb.sbid,dw,dpjgsl", 
				"em_jgdyb.jgdyid = ?", "sbmc,dw", new Object[]{jgdyid} );
		bundle.put("djsm",  dataset.getList());
	}
	
	public void query_jgdyb(Parameters parameters, Bundle bundle) {
//		String jgdyid = parameters.getString("jgdyid");
		Dataset dataset = Sys.query("em_jgdyb", " zzjgid, jgdyid, jgdybh, jgdymc, jgdyms, bdgkjbh "," 1=1 ", null, new Object[]{});
		bundle.put("jgdy",  dataset.getList());
	}
}







