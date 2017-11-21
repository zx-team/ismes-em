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
 * 能力组 activity
 */
public class AbilityGroupActivity {

	private Logger log4j = Logger.getLogger(AbilityGroupActivity.class);
	
	public String index(Parameters parameters,Bundle bundle){
		return "nlz";
	}
	
	/**
	 * 能力组表格
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	public String agTable(Parameters parameters,Bundle bundle){
		int page = parameters.get("page") == null ? 1 : parameters.getInteger("page");
		int pageSize = parameters.get("pageSize") == null ? 100 : parameters.getInteger("pageSize");
		
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
				.append("sbbh").append(SqlConstant.COMMA);//设备编号
		String fields = fieldsSb.deleteCharAt(fieldsSb.length() - 1).toString();
		String groupby = null;
		String orderby = null;
		
		//外层能力组的条件
		StringBuffer condition = new StringBuffer(SqlConstant.CONDITION_TRUE);
		List<String> values = new ArrayList<String>();
		//能力组名称
		String nlzmc = parameters.getString("nlzmc");
		if(StringUtils.isNotBlank(nlzmc)){
			condition = condition.append(SqlConstant.AND).append(TableConstant.能力组).append(SqlConstant.PERIOD)
					.append("nlzmc").append(SqlConstant.LIKE).append(SqlConstant.QUESTION_MARK);
			values.add("%"+nlzmc+"%");
		}
		//能力组编号
		String nlzbh = parameters.getString("nlzbh");
		if(StringUtils.isNotBlank(nlzbh)){
			condition = condition.append(SqlConstant.AND).append(TableConstant.能力组).append(SqlConstant.PERIOD)
					.append("nlzbh").append(SqlConstant.LIKE).append(SqlConstant.QUESTION_MARK);
			values.add("%"+nlzbh+"%");
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
				condition = condition.append(SqlConstant.AND).append(TableConstant.能力组).append(SqlConstant.PERIOD)
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
					String id = map.get("nlzid").toString();
					ids = ids + id + "','";
				}
				ids = ids.substring(0, ids.length()-2);
				condition = condition.append(SqlConstant.AND).append(TableConstant.能力组).append(SqlConstant.PERIOD)
						.append("nlzid").append(" in (").append(ids).append(")");
			}else{
				condition = condition.append(" and 1 = 0 ");
			}
		}
		
		String groupBy = null;
		String orderBy = null;
		Dataset	dataset =  Sys.query(TableConstant.能力组,"nlzid,"/*能力组id*/+ "nlzmc,"/*能力组名称*/+"zzjgid,"/*组织机构id*/
				+ "nlzbh,"/*能力组编号*/+"nlzms"/*能力组描述*/,condition.toString(),groupBy,orderBy,(page-1)*pageSize,pageSize,values.toArray());
		List<Map<String,Object>> agList = dataset.getList();
		if(CollectionUtils.isNotEmpty(agList)){
			// 为表格添加  设备信息
			String conditions  = TableConstant.能力组设备关联表 +SqlConstant.PERIOD + "nlzid = ? ";
			for(Map<String,Object> map : agList){
				String nlzid = map.get("nlzid").toString();
				Dataset data = Sys.query(models, join, fields, conditions, groupby,orderby, new Object[]{nlzid});
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
		bundle.put("totalRecord", dataset.getCount());
		return "json:";
	}
	
	/**
	 * 根据能力组id查询设备信息
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	public String getSbxxByNlzid(Parameters parameters,Bundle bundle){
		String nlzid = parameters.getString("nlzid");
		if(StringUtils.isBlank(nlzid)){
			bundle.put("list", null);
			return "json:";
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
				.append("sbbh").append(SqlConstant.COMMA);//设备编号
		String fields = fieldsSb.deleteCharAt(fieldsSb.length() - 1).toString();
		Dataset data = Sys.query(models, join, fields, " nlzid = ? ", null ,null, new Object[]{nlzid});
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
	 * 能力组删除
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	public String nlzDelete(Parameters parameters,Bundle bundle){
		String canDeleteMcs = "";
		List<Object[]> deleteConditionValues = new ArrayList<Object[]>();
		JSONArray jsonarray = JSONArray.fromObject((String) parameters.get("nlz_list"));  
		for (int i = 0; i < jsonarray.size(); i++) {
			Map<String, Object> map = jsonarray.getJSONObject(i);
			//根据能力组id查询，如果有关联不能查询，将id和名称记录 
			String nlzid = map.get("nlzid").toString();
			parameters.set("nlzid", nlzid);
			Bundle b = Sys.callModuleService("pm", "pmservice_gxxx_by_nlzid", parameters);
			if(b == null){
				continue;
			}
			List<Map<String,Object>> list = (List<Map<String, Object>>) b.get("gxList");
			if(list == null){
				continue;
			}
			//如果能力组已经被使用，不能删除
			if(list.size()>0){
				canDeleteMcs = canDeleteMcs + map.get("nlzmc").toString() + "; ";
				continue;
			}
			
			deleteConditionValues.add(new Object[]{nlzid});
//			Sys.delete(TableConstant.能力组, "nlzid = ? ", nlzid);
		}  
		Sys.delete(TableConstant.能力组, "nlzid = ? ", deleteConditionValues);
		bundle.put("canDeleteMcs", canDeleteMcs);
		return "json:";
	}
	
	public void nlzEdit(Parameters parameters,Bundle bundle){
		
	}
	
	/**
	 * 能力组信息保存
	 * @param parameters
	 * @param bundle
	 */
	public void nlzSave(Parameters parameters,Bundle bundle){
		String nlzbh = parameters.getString("nlzbh");
		String nlzmc = parameters.getString("nlzmc");
		String nlzms = parameters.getString("nlzms");
		String zzjgid = parameters.getString("sscj");
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("nlzbh", nlzbh);
		map.put("nlzmc", nlzmc);
		map.put("nlzms", nlzms);
		map.put("zzjgid",zzjgid);
		
		Sys.insert(TableConstant.能力组, map);
		String nlzid = map.get("nlzid").toString();
		JSONArray jsonarray = JSONArray.fromObject((String) parameters.get("nlz_sb_list"));  
		List<Map<String,Object>> nlz_sb_list = new ArrayList<Map<String,Object>>();
		for (int i = 0; i < jsonarray.size(); i++) {
			Map<String,Object> nlz_sb_map = new HashMap<String, Object>();
			//得到设备id
			Map<String, Object> m = jsonarray.getJSONObject(i);
			nlz_sb_map.put("sbid", m.get("sbid").toString());
			nlz_sb_map.put("nlzid", nlzid);
			nlz_sb_list.add(nlz_sb_map);
		}
		Sys.insert(TableConstant.能力组设备关联表, nlz_sb_list);
	}
	
	
	/**
	 * 能力组信息编辑
	 * @param parameters
	 * @param bundle
	 */
	public void nlzUpdate(Parameters parameters,Bundle bundle){
		String nlzid = parameters.getString("nlzid");
		String nlzmc = parameters.getString("nlzmc");
		String nlzms = parameters.getString("nlzms");
		String nlzbh = parameters.getString("nlzbh");
		String zzjgid = parameters.getString("sscj");
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("nlzmc", nlzmc);
		map.put("nlzms", nlzms);
		map.put("nlzbh", nlzbh);
		map.put("zzjgid",zzjgid);
		Sys.update(TableConstant.能力组, map, " nlzid = ? ", new Object[]{nlzid});
		
		//当前有哪些设备
		Dataset dataset = Sys.query(TableConstant.能力组设备关联表, "sbid", " nlzid = ? ", null, new Object[]{nlzid});
		List<Map<String,Object>> sbList = dataset.getList();
		Set<String> nowSet = new HashSet<String>();
		for(Map<String,Object> m : sbList){
			nowSet.add(m.get("sbid").toString());
		}
		
		//页面上的设备
		Set<String> pageSet =  new HashSet<String>(); 
		JSONArray jsonarray = JSONArray.fromObject((String) parameters.get("nlz_sb_list"));  
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
			m.put("nlzid", nlzid);
			insertList.add(m);
		}
		Sys.insert(TableConstant.能力组设备关联表, insertList);
		
		//原来的 减去 交集  是删除的
		List<Object[]> deleteList = new ArrayList<Object[]>();
		nowSet.removeAll(set);
		for(String  str : nowSet){
			deleteList.add(new Object[]{str,nlzid});
		}
		Sys.delete(TableConstant.能力组设备关联表, " sbid = ? and nlzid = ?", deleteList);
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
