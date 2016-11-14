package com.isesol.mes.ismes.em.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.isesol.ismes.platform.core.service.bean.Dataset;
import com.isesol.ismes.platform.module.Bundle;
import com.isesol.ismes.platform.module.Parameters;
import com.isesol.ismes.platform.module.Sys;

import net.sf.json.JSONArray;

/**
 * 设备类型管理
 * @author Yang Fan
 *
 */
public class EquipmentTypeActivity {
	/**
	 * 跳转设备类型页面
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	public String query_sblx(Parameters parameters, Bundle bundle) {
		return "em_sblx";
	}
	
	/**
	 * 查询设备类型信息
	 * 
	 * @param parameters
	 * @param bundle
	 * @return
	 * @throws Exception
	 */
	public String table_sblx(Parameters parameters, Bundle bundle) throws Exception {
		
		// 查询设备信息表
		String query_sblxbh = parameters.getString("query_sblxbh");
		String query_sblxmc = parameters.getString("query_sblxmc");
		String query_phxh = parameters.getString("query_phxh");
		String query_nlms = parameters.getString("query_nlms");
		String query_sbfl = parameters.getString("query_sbfl");
		String query_skxt = parameters.getString("query_skxt");
		String query_qybz = parameters.getString("query_qybz");
		String sortName = parameters.getString("sortName");// 排序字段
		String sortOrder = parameters.getString("sortOrder");// 排序方式 asc desc

		String con = " 1 = 1 ";
		List<Object> val = new ArrayList<Object>();
		if (StringUtils.isNotBlank(query_sblxbh)) {
			con = con + " and sblxbh like ? ";
			val.add("%" + query_sblxbh + "%");
		}
		if (StringUtils.isNotBlank(query_sblxmc)) {
			con = con + " and sblxmc like ? ";
			val.add("%" + query_sblxmc + "%");
		}
		if (StringUtils.isNotBlank(query_nlms)) {
			con = con + " and nlms like ? ";
			val.add("%" + query_nlms + "%");
		}
		if (StringUtils.isNotBlank(query_phxh)) {
			con = con + " and xhph like ? ";
			val.add("%" + query_phxh + "%");
		}
		if (StringUtils.isNotBlank(query_sbfl)) {
			con = con + " and sbfldm = ? ";
			val.add(query_sbfl);
		}
		if (StringUtils.isNotBlank(query_skxt)) {
			con = con + " and skxtdm = ? ";
			val.add(query_skxt);
		}
		if (StringUtils.isNotBlank(query_qybz)) {
			con = con + " and qybsdm = ? ";
			val.add(query_qybz);
		}
		if (StringUtils.isNotBlank(sortName)) {
			sortOrder = sortName + " " + sortOrder + " ";
		} else {
			sortOrder = "sblxid desc";
		}
		
		
		// 查询库存信息
		int page = Integer.parseInt(parameters.get("page").toString());
		int pageSize = Integer.parseInt(parameters.get("pageSize").toString());
		Dataset datasetsblx = Sys.query("em_sblxb", " sblxid,sblxbh,sblxmc,xhph,skxtdm,nlms,qybsdm,sbfldm ", con, sortOrder,
				(page - 1) * pageSize, pageSize, val.toArray());
		List<Map<String, Object>> sblx = datasetsblx.getList();
		bundle.put("rows", sblx);
		int totalPage = datasetsblx.getTotal() % pageSize == 0 ? datasetsblx.getTotal() / pageSize :datasetsblx.getTotal() / pageSize + 1;
		bundle.put("totalPage", totalPage);
		bundle.put("currentPage", page);
		bundle.put("totalRecord", datasetsblx.getTotal());
		return "json:";
	}
	/**
	 * 新增或者修改设备类型信息
	 * 
	 * @param parameters
	 * @param bundle
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public String update_sblx(Parameters parameters, Bundle bundle) throws Exception {

		List<Map<String, Object>> sblx_inlist = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> sblx_uplist = new ArrayList<Map<String, Object>>();
		List<Object[]> objlist = new ArrayList<Object[]>();
		
		StringBuffer sblxbhSB = new StringBuffer();
		sblxbhSB.append("(");
		JSONArray jsonarray = JSONArray.fromObject(parameters.get("data_list"));
		for (int i = 0; i < jsonarray.size(); i++) {
			Map<String, Object> map = new HashMap<String, Object>();
			Map<String, Object> map_in = new HashMap<String, Object>();
			map = jsonarray.getJSONObject(i);
			if (StringUtils.isNotBlank(map.get("addSign").toString())) {
				
				Dataset ds = Sys.query("em_sblxb", "sblxbh", "sblxbh=?", null, new Object[]{map.get("sblxbh")});
				if(null != ds && ds.getCount() > 0){
					bundle.put("code", "1");//0:成功，1:失败
					bundle.put("message", "设备类型编号重复，请重新输入！");
					return "json:";
				}
				
				//map_in.put("sblxid", map.get("sblxid"));
				map_in.put("sblxbh", map.get("sblxbh"));
				map_in.put("sblxmc", map.get("sblxmc"));
				map_in.put("xhph", map.get("xhph"));
				map_in.put("skxtdm", map.get("skxtdm"));
				map_in.put("nlms", map.get("nlms"));
				map_in.put("qybsdm", map.get("qybsdm"));
				map_in.put("sbfldm", map.get("sbfldm"));
				sblxbhSB.append("'").append(map.get("sblxbh")).append("',");

				sblx_inlist.add(map_in);
			} else {
				Dataset ds = Sys.query("em_sblxb", "sblxbh", "sblxbh=? and sblxid<>?", null, new Object[]{map.get("sblxbh"),map.get("sblxid")});
				if(null != ds && ds.getCount() > 0){
					bundle.put("code", "1");//0:成功，1:失败
					bundle.put("message", "设备类型编号重复，请重新输入！");
					return "json:";
				}
				
				objlist.add(new Object[] { map.get("sblxid") });
				map_in.put("sblxbh", map.get("sblxbh"));
				map_in.put("sblxmc", map.get("sblxmc"));
				map_in.put("xhph", map.get("xhph"));
				map_in.put("skxtdm", map.get("skxtdm"));
				map_in.put("nlms", map.get("nlms"));
				//int类型为空时，异常
				if(null != map.get("qybsdm") && !"".equals(map.get("qybsdm"))){
					map_in.put("qybsdm", map.get("qybsdm"));
				}
				map_in.put("sbfldm", map.get("sbfldm"));
				sblx_uplist.add(map_in);
			}
		}
		sblxbhSB.deleteCharAt(sblxbhSB.length()-1);
		sblxbhSB.append(")");
		
		if(sblx_inlist.size()>0 ){
			Dataset datasetsblx = Sys.query("em_sblxb", "sblxbh", "sblxbh in " + sblxbhSB.toString(), null, new Object[]{});
			if(datasetsblx.getCount()>0){
				throw new Exception("设备类型编号重复！"); 
			}
		}
		
		if (sblx_inlist.size() > 0) {
			try {
				int i = Sys.insert("em_sblxb", sblx_inlist);
				System.out.println("新增设备类型信息 数量：" + i );
			} catch (Exception e) {
				e.printStackTrace();
				throw new Exception("新增设备类型信息失败！", e);
			}
		}
		if (sblx_uplist.size() > 0) {
			try {
				int i = Sys.update("em_sblxb", sblx_uplist, "sblxid=?", objlist);
				System.out.println("更新设备类型信息 数量：" + i );
			} catch (Exception e) {
				e.printStackTrace();
				throw new Exception("更新设备类型信息失败！", e);
			}
		}
		bundle.put("code", "0");//0:成功，1:失败
		return "json:";
	}

	/**
	 * 删除设备类型信息
	 * 
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	public String del_sblx(Parameters parameters, Bundle bundle)  throws Exception{
		if (StringUtils.isBlank(parameters.getString("data_list"))) {
			return "json:";
		}
		List<Object[]> objlist = new ArrayList<Object[]>();
		objlist.add(new Object[] { Integer.parseInt(parameters.getString("data_list")) });
		try {
			int i = Sys.delete("em_sblxb", "sblxid=?", objlist);
			System.out.println("删除设备类型信息 数量：" + i + " 设备类型id：" + String.valueOf(objlist.get(0)[0]));
		} catch (Exception e) {
			e.printStackTrace();
			throw  new Exception("删除设备类型信息失败！",e);
		}
		return "json:";
	}
	
	
}
