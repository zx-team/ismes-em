package com.isesol.mes.ismes.em.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.isesol.ismes.platform.core.service.bean.Dataset;
import com.isesol.ismes.platform.module.Bundle;
import com.isesol.ismes.platform.module.Parameters;
import com.isesol.ismes.platform.module.Sys;
import com.isesol.mes.ismes.em.constant.SqlConstant;
import com.isesol.mes.ismes.em.constant.TableConstant;

import net.sf.json.JSONArray;

/**
 * 加工单元activity
 */
public class JgdyActivity {

	private Logger log4j = Logger.getLogger(JgdyActivity.class);
	
	public String index(Parameters parameters,Bundle bundle){
		return "jgdy";
	}
	
	/**
	 * 加工单元表格
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	public String agTable(Parameters parameters,Bundle bundle){
		int page = parameters.get("page") == null ? 1 : parameters.getInteger("page");
		int pageSize = parameters.get("pageSize") == null ? 100 : parameters.getInteger("pageSize");
		
		//关联表的条件
		String[] models = new String[]{TableConstant.设备信息表,TableConstant.加工单元设备关联表};
		String join = new StringBuffer()
				.append(TableConstant.加工单元设备关联表).append(SqlConstant.JOIN)
				.append(TableConstant.设备信息表).append(SqlConstant.ON)
				.append(TableConstant.加工单元设备关联表).append(SqlConstant.PERIOD).append("sbid")
				.append(SqlConstant.EQUALS)
				.append(TableConstant.设备信息表).append(SqlConstant.PERIOD).append("sbid").toString();
		StringBuffer fieldsSb = new StringBuffer()
				.append(TableConstant.加工单元设备关联表).append(SqlConstant.PERIOD)
				.append("jgdyid").append(SqlConstant.COMMA)//设备id
				.append(TableConstant.设备信息表).append(SqlConstant.PERIOD)
				.append("sbid").append(SqlConstant.COMMA)//设备id
				.append(TableConstant.设备信息表).append(SqlConstant.PERIOD)
				.append("sbmc").append(SqlConstant.COMMA)//设备名称 
				.append(TableConstant.设备信息表).append(SqlConstant.PERIOD)
				.append("sbbh").append(SqlConstant.COMMA);//设备编号
		String fields = fieldsSb.deleteCharAt(fieldsSb.length() - 1).toString();
		String groupby = null;
		String orderby = null;
		
		//外层加工单元的条件
		StringBuffer condition = new StringBuffer(SqlConstant.CONDITION_TRUE);
		List<String> values = new ArrayList<String>();
		//加工单元名称
		String jgdymc = parameters.getString("jgdymc");
		if(StringUtils.isNotBlank(jgdymc)){
			condition = condition.append(SqlConstant.AND).append(TableConstant.加工单元).append(SqlConstant.PERIOD)
					.append("jgdymc").append(SqlConstant.LIKE).append(SqlConstant.QUESTION_MARK);
			values.add("%"+jgdymc+"%");
		}
		//加工单元编号
		String jgdybh = parameters.getString("jgdybh");
		if(StringUtils.isNotBlank(jgdybh)){
			condition = condition.append(SqlConstant.AND).append(TableConstant.加工单元).append(SqlConstant.PERIOD)
					.append("jgdybh").append(SqlConstant.LIKE).append(SqlConstant.QUESTION_MARK);
			values.add("%"+jgdybh+"%");
		}
		//所属车间
		String sscj = parameters.getString("sscj");
		if(StringUtils.isNotBlank(sscj)){
			parameters.set("zzjgmc", sscj);
			Bundle b = Sys.callModuleService("org", "cjQueryService", parameters);
			List<Map<String,Object>> listCj = (List<Map<String, Object>>) b.get("data");
			if(CollectionUtils.isNotEmpty(listCj)){
				String zzjgids = "'";
				for(Map<String,Object> m : listCj){
					String zzjgid = m.get("zzjgid").toString();
					zzjgids = zzjgids + zzjgid + "','";
				}
				zzjgids = zzjgids.substring(0, zzjgids.length()-2);
				condition = condition.append(SqlConstant.AND).append(TableConstant.加工单元).append(SqlConstant.PERIOD)
						.append("zzjgid").append(" in (").append(zzjgids).append(")");
			}else{
				condition = condition.append(" and 1 = 0 ");
			}
		}
		//设备名称
		String sbmc = parameters.getString("sbmc");
		if(StringUtils.isNotBlank(sbmc)){
			String conditions  = TableConstant.设备信息表 +SqlConstant.PERIOD + "sbmc" + SqlConstant.LIKE + " ? ";
			Dataset data = Sys.query(models, join, fields, conditions, groupby,orderby, new Object[]{"%"+sbmc+"%"});
			if(CollectionUtils.isNotEmpty(data.getList())){
				String ids = "'";
				for(Map<String,Object> map : data.getList()){
					String id = map.get("jgdyid").toString();
					ids = ids + id + "','";
				}
				ids = ids.substring(0, ids.length()-2);
				condition = condition.append(SqlConstant.AND).append(TableConstant.加工单元).append(SqlConstant.PERIOD)
						.append("jgdyid").append(" in (").append(ids).append(")");
			}else{
				condition = condition.append(" and 1 = 0 ");
			}
		}
		//绑定工控机编号
		String bdgkjbh = parameters.getString("bdgkjbh");
		if(StringUtils.isNotBlank(bdgkjbh)){
			condition = condition.append(SqlConstant.AND).append(TableConstant.加工单元).append(SqlConstant.PERIOD)
					.append("bdgkjbh").append(SqlConstant.LIKE).append(SqlConstant.QUESTION_MARK);
			values.add("%"+bdgkjbh+"%");
		}
		
		String groupBy = null;
		String orderBy = "jgdybh asc";
		Dataset	dataset =  Sys.query(TableConstant.加工单元,"jgdyid,"/*加工单元id*/+ "jgdymc,"/*加工单元名称*/+"zzjgid,"/*组织机构id*/
				+ "jgdybh,"/*加工单元编号*/+"bdgkjbh,"/*绑定工控机编号*/+"jgdyms"/*加工单元描述*/,condition.toString(),groupBy,orderBy,(page-1)*pageSize,pageSize,values.toArray());
		List<Map<String,Object>> agList = dataset.getList();
		if(CollectionUtils.isNotEmpty(agList)){
			// 为表格添加  设备信息
			String conditions  = TableConstant.加工单元设备关联表 +SqlConstant.PERIOD + "jgdyid = ? ";
			for(Map<String,Object> map : agList){
				String jgdyid = map.get("jgdyid").toString();
				Dataset data = Sys.query(models, join, fields, conditions, groupby,orderby, new Object[]{jgdyid});
				List<Map<String,Object>> sbList = data.getList();
				String sssb = "";
				if(CollectionUtils.isNotEmpty(sbList)){
					for(Map<String,Object> m : sbList){
						if( m.get("sbmc") == null){
							continue;
						}
						String mc = (String) m.get("sbmc");
						if(StringUtils.isEmpty(mc)){
							continue;
						}
						sssb = sssb + mc + "; ";
					}
				}
				//所选设备
				map.put("sssb", sssb);
				
				//添加组织机构名称
				if(map.get("zzjgid") != null ){
					parameters.set("id", map.get("zzjgid"));
					Bundle b = Sys.callModuleService("org", "nameService", parameters);
					if(b!=null){
						map.put("sscj", b.get("name"));
					}
				}
			}
		}
		
		bundle.put("rows", agList);
		int totalPage = dataset.getTotal() % pageSize== 0 ? dataset.getTotal()/pageSize:
			dataset.getTotal()/pageSize+1;
		bundle.put("totalPage", totalPage);
		bundle.put("currentPage", page);
		bundle.put("totalRecord", dataset.getTotal());
		return "json:";
	}
	
	/**
	 * 根据加工单元id查询设备信息
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	public String getSbxxByJgdyid(Parameters parameters,Bundle bundle){
		String jgdyid = parameters.getString("jgdyid");
		if(StringUtils.isBlank(jgdyid)){
			bundle.put("list", null);
			return "json:";
		}
		//关联表的条件
		String[] models = new String[]{TableConstant.设备信息表,TableConstant.加工单元设备关联表};
		String join = new StringBuffer()
				.append(TableConstant.加工单元设备关联表).append(SqlConstant.JOIN)
				.append(TableConstant.设备信息表).append(SqlConstant.ON)
				.append(TableConstant.加工单元设备关联表).append(SqlConstant.PERIOD).append("sbid")
				.append(SqlConstant.EQUALS)
				.append(TableConstant.设备信息表).append(SqlConstant.PERIOD).append("sbid").toString();
		StringBuffer fieldsSb = new StringBuffer()
				.append(TableConstant.加工单元设备关联表).append(SqlConstant.PERIOD)
				.append("jgdyid").append(SqlConstant.COMMA)//设备id
				.append(TableConstant.设备信息表).append(SqlConstant.PERIOD)
				.append("sbid").append(SqlConstant.COMMA)//设备id
				.append(TableConstant.设备信息表).append(SqlConstant.PERIOD)
				.append("sbmc").append(SqlConstant.COMMA)//设备名称 
				.append(TableConstant.设备信息表).append(SqlConstant.PERIOD)
				.append("sbbh").append(SqlConstant.COMMA);//设备编号
		String fields = fieldsSb.deleteCharAt(fieldsSb.length() - 1).toString();
		Dataset data = Sys.query(models, join, fields, " jgdyid = ? ", null ,null, new Object[]{jgdyid});
		List<Map<String,Object>> sbList = data.getList();
		bundle.put("list", sbList);
		return "json:";
	}
	
	/**
	 * 根据设备id得到设备信息
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	public String getSbxxBySbid(Parameters parameters,Bundle bundle){
		String sbid = parameters.getString("sbid");
		if(StringUtils.isBlank(sbid)){
			bundle.put("sbxx", null);
			return "json:";
		}
		Dataset dataset = Sys.query(TableConstant.设备信息表, "sbid,sbmc,sbbh", " sbid = ? ", null , new Object[]{sbid});
		Map<String,Object> sbxx = dataset.getMap();
		bundle.put("sbxx", sbxx);
		return "json:";
	}
	
	/**
	 * 加工单元删除
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	public String jgdyDelete(Parameters parameters, Bundle bundle) {
		String canDeleteMcs = "";
		List<Object[]> deleteConditionValues = new ArrayList<Object[]>();
		JSONArray jsonarray = JSONArray.fromObject((String) parameters.get("jgdy_list"));
		for (int i = 0; i < jsonarray.size(); i++) {
			Map<String, Object> map = jsonarray.getJSONObject(i);
			// 查询是否有工序组关联加工单元，如果有就不能被删除
			String jgdyid = map.get("jgdyid").toString();
			parameters.set("jgdyid", jgdyid);
			Bundle b = Sys.callModuleService("pm", "pmservice_query_gxz_by_jgdyid", parameters);
			if (b == null) {
				continue;
			}
			List<Map<String, Object>> list = (List<Map<String, Object>>) b.get("gxzList");
			if (list == null) {
				continue;
			}
			// 如果加工单元已经被使用，不能删除
			if (list.size() > 0) {
				canDeleteMcs = canDeleteMcs + map.get("jgdymc").toString() + "; ";
				continue;
			}
			deleteConditionValues.add(new Object[] { jgdyid });
		}
		Sys.delete(TableConstant.加工单元, "jgdyid = ? ", deleteConditionValues);
		bundle.put("canDeleteMcs", canDeleteMcs);
		return "json:";
	}
	
	public void jgdyEdit(Parameters parameters,Bundle bundle){
		
	}
	
	/**
	 * 加工单元信息保存
	 * @param parameters
	 * @param bundle
	 */
	public void jgdySave(Parameters parameters,Bundle bundle){
		String jgdybh = parameters.getString("jgdybh");
		String jgdymc = parameters.getString("jgdymc");
		String jgdyms = parameters.getString("jgdyms");
		String zzjgid = parameters.getString("sscj");
		String bdgkjbh = parameters.getString("bdgkjbh");
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("jgdybh", jgdybh);
		map.put("jgdymc", jgdymc);
		map.put("jgdyms", jgdyms);
		map.put("zzjgid",zzjgid);
		map.put("bdgkjbh",bdgkjbh);
		
		Sys.insert(TableConstant.加工单元, map);
		String jgdyid = map.get("jgdyid").toString();
		JSONArray jsonarray = JSONArray.fromObject((String) parameters.get("jgdy_sb_list"));  
		List<Map<String,Object>> jgdy_sb_list = new ArrayList<Map<String,Object>>();
		for (int i = 0; i < jsonarray.size(); i++) {
			Map<String,Object> jgdy_sb_map = new HashMap<String, Object>();
			//得到设备id
			Map<String, Object> m = jsonarray.getJSONObject(i);
			jgdy_sb_map.put("sbid", m.get("sbid").toString());
			jgdy_sb_map.put("jgdyid", jgdyid);
			jgdy_sb_list.add(jgdy_sb_map);
		}
		Sys.insert(TableConstant.加工单元设备关联表, jgdy_sb_list);
	}
	
	
	/**
	 * 加工单元信息编辑
	 * @param parameters
	 * @param bundle
	 */
	public void jgdyUpdate(Parameters parameters,Bundle bundle){
		String jgdyid = parameters.getString("jgdyid");
		String jgdymc = parameters.getString("jgdymc");
		String jgdyms = parameters.getString("jgdyms");
		String jgdybh = parameters.getString("jgdybh");
		String zzjgid = parameters.getString("sscj");
		String bdgkjbh = parameters.getString("bdgkjbh");
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("jgdymc", jgdymc);
		map.put("jgdyms", jgdyms);
		map.put("jgdybh", jgdybh);
		map.put("zzjgid",zzjgid);
		map.put("bdgkjbh",bdgkjbh);
		Sys.update(TableConstant.加工单元, map, " jgdyid = ? ", new Object[]{jgdyid});
		
		//当前有哪些设备
		Dataset dataset = Sys.query(TableConstant.加工单元设备关联表, "sbid", " jgdyid = ? ", null, new Object[]{jgdyid});
		List<Map<String,Object>> sbList = dataset.getList();
		Set<String> nowSet = new HashSet<String>();
		for(Map<String,Object> m : sbList){
			nowSet.add(m.get("sbid").toString());
		}
		
		//页面上的设备
		Set<String> pageSet =  new HashSet<String>(); 
		JSONArray jsonarray = JSONArray.fromObject((String) parameters.get("jgdy_sb_list"));  
		for (int i = 0; i < jsonarray.size(); i++) {
			//得到设备id
			Map<String, Object> m = jsonarray.getJSONObject(i);
			pageSet.add( m.get("sbid").toString());
		}
		//交集 --这些是不用动的
		Set<String> set = new HashSet<String>();
		set.addAll(nowSet);
		set.retainAll(pageSet);
		
		List<Map<String,Object>> insertList = new ArrayList<Map<String,Object>>();
		//页面上  减去交集  是新增的
		pageSet.removeAll(set);
		for(String  str : pageSet){
			Map<String,Object> m = new HashMap<String, Object>();
			m.put("sbid", str);
			m.put("jgdyid", jgdyid);
			insertList.add(m);
		}
		if (!insertList.isEmpty()) {
			Sys.insert(TableConstant.加工单元设备关联表, insertList);
		}
		
		//原来的 减去 交集  是删除的
		List<Object[]> deleteList = new ArrayList<Object[]>();
		nowSet.removeAll(set);
		for(String  str : nowSet){
			deleteList.add(new Object[]{str,jgdyid});
		}
		if (!deleteList.isEmpty()) {
			Sys.delete(TableConstant.加工单元设备关联表, " sbid = ? and jgdyid = ?", deleteList);
		}
	}
	
	/**
	 * 选择设备型号
	 * @param parameters
	 * @param bundle
	 */
	public String sbxhSelect(Parameters parameters,Bundle bundle){
		String sbfldm = parameters.getString("parent");
		if(StringUtils.isEmpty(sbfldm)){
			log4j.info("层级查询设备型号，设备分类代码不能为空");
			bundle.put("sbxh", new Object[]{});
			return "json:sbxh";
		}
		Dataset dataset = Sys.query(TableConstant.设备类型表, "sblxid,sblxmc", " sbfldm = ? ", null, new Object[]{sbfldm});
		if(dataset == null){
			log4j.info("根据设备分类代码 ==" + sbfldm + " 查询设备类型 ，没有结果集返回");
			bundle.put("sbxh", new Object[]{});
			return "json:sbxh";
		}
		List<Map<String,Object>> list = dataset.getList();
		if(CollectionUtils.isEmpty(list)){
			log4j.info("根据设备分类代码 ==" + sbfldm + " 查询设备类型 ，没有结果集返回");
			bundle.put("sbxh", new Object[]{});
			return "json:sbxh";
		}
		List<Map<String,Object>> returnList = new ArrayList<Map<String,Object>>();
		for(Map<String,Object> map : list){
			Map<String,Object> m = new HashMap<String, Object>();
			m.put("label", map.get("sblxmc"));
			m.put("value", map.get("sblxid"));
			returnList.add(m);
		}
		bundle.put("sbxh", returnList.toArray());
		return "json:sbxh";
	}
	
	/**
	 * 根据选择的设备类型   得到所有设备
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	public String sbxxBySblxid(Parameters parameters,Bundle bundle){
		String sblxid = parameters.getString("sbxhid");
		String zzjgid = parameters.getString("zzjgid");
	
		StringBuffer condition = new StringBuffer(" 1 = 1 ");
		List<Object> conditionValue = new ArrayList<Object>();
		if(StringUtils.isNotBlank(sblxid)){
			condition = condition.append(" and sblxid = ? ");
			conditionValue.add(sblxid);
		}
		
		if(StringUtils.isNotBlank(zzjgid)){
			condition = condition.append(" and zzjgid = ? ");
			conditionValue.add(zzjgid);
		}
		
		Dataset dataset = Sys.query(TableConstant.设备信息表, "sbid,sbmc", condition.toString(),
				"jyrq", conditionValue.toArray());
		List<Map<String,Object>> list = dataset.getList();
		List<Map<String,Object>> returnList = new ArrayList<Map<String,Object>>();
		if(CollectionUtils.isNotEmpty(list)){
			for(Map<String,Object> map : list){
				Map<String,Object> m = new HashMap<String, Object>();
				m.put("label", map.get("sbmc"));
				m.put("value", map.get("sbid"));
				returnList.add(m);
			}
		}
		bundle.put("select_sb", returnList.toArray());
		return "json:select_sb";
	}
	
	public String sscjSelect(Parameters parameters,Bundle bundle){
		Bundle b = Sys.callModuleService("org", "cjService", parameters);
		if(b == null){
			log4j.info("查询所属车间出现异常，没有返回结果集");
			bundle.put("select_sscj", new Object[]{});
			return "json:sscj";
		}
		List<Map<String,Object>> cjList = (List<Map<String, Object>>) b.get("data");
		List<Map<String,Object>> returnList = new ArrayList<Map<String,Object>>();
		for(Map<String,Object> map : cjList){
			Map<String,Object> m = new HashMap<String, Object>();
			m.put("label", map.get("zzjgmc"));
			m.put("value", map.get("zzjgid"));
			returnList.add(m);
		}
		bundle.put("select_sscj", returnList.toArray());
		return "json:select_sscj";
	}
}
