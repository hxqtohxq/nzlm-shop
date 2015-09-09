package com.earl.solrj;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanMap;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import com.earl.solrj.query.QueryBuilder;
import com.earl.solrj.query.filters.FacetFilter;
import com.earl.solrj.query.pojo.GoodsVo;
import com.earl.solrj.server.SolrServerFactory;

/**
 * solr的service层.
 */
public class SolrService {

	private SolrServerFactory masterFactory = new SolrServerFactory();

	/**
	 * 添加对象索引.
	 * 
	 * @param goods
	 *            商品对象.
	 * @throws Exception
	 */
	public void addBeanIndex(GoodsVo goods) throws Exception {

		SolrClient updateClient = this.masterFactory.getUpdateSolrClient();
		//
		updateClient.addBean(goods);
		//
		updateClient.commit();
	}

	/**
	 * 通过id删除对象.
	 * 
	 * @param goods
	 *            商品id.
	 * @throws Exception
	 */
	public void deleteById(GoodsVo goods) throws Exception {
		SolrClient updateClient = this.masterFactory.getUpdateSolrClient(true);
		String id = goods.getId();
		updateClient.deleteById(id.toString());
		updateClient.commit();
	}

	/**
	 * 通过查询条件删除索引.
	 * 
	 * @param query
	 *            索引条件.
	 * @throws Exception
	 */
	public void deleteByQuery(String query) throws Exception {
		SolrClient updateClient = this.masterFactory.getUpdateSolrClient(true);

		// updateClient.deleteByQuery("description: describemc");
		updateClient.deleteByQuery(query);
		updateClient.commit();
	}

	/**
	 * 查询函数，并且将结果封装成对象
	 * 
	 * @param queryStatement
	 *            查询语句.
	 * @return beans 对象集合.
	 * @throws Exception
	 */
	private List<GoodsVo> queryBeans(SolrQuery queryStatement) throws Exception {
		SolrClient queryClient = this.masterFactory.getQuerySolrClient();

		QueryResponse query = queryClient.query(queryStatement);
		List<GoodsVo> beans = query.getBeans(GoodsVo.class);
		return beans;
	}

	/**
	 * 查询函数，并且结果不封装成对象
	 * 
	 * @param queryStatement
	 *            查询语句.
	 * @return results 对象list.
	 * @throws Exception
	 */
	public SolrDocumentList queryList(SolrQuery queryStatement)
			throws Exception {
		SolrClient queryClient = this.masterFactory.getQuerySolrClient();

		QueryResponse query = queryClient.query(queryStatement);
		SolrDocumentList results = query.getResults();
		return results;
	}

	/**
	 * 通过对象非空属性查询符合对象.
	 * 
	 * @param goods
	 *            对象.
	 * @return beans 结果集.
	 * @throws Exception
	 */
	public List<GoodsVo> queryBeans(GoodsVo goods) throws Exception {
		// 得到非空属性
		Map<String, Object> notNullProperties = getNotNullProperties(goods);
		// 得到查询语句 // OR连接查询语句
		String queryString = getQueryStringWithOR(notNullProperties);

		SolrQuery query = new SolrQuery(queryString.toString());
		List<GoodsVo> beans = queryBeans(query);

		return beans;
	}

	/**
	 * 通过类别和属性查询商品.
	 * 
	 * @param type2
	 *            商品的类别.
	 * @param list
	 *            商品的属性.
	 * @return beans 结果集.
	 * @throws Exception
	 */
	public List<GoodsVo> queryBeans(GoodsVo goods, List<String> list)
			throws Exception {
		SolrQuery query = new SolrQuery();
		Iterator<String> iterator = list.iterator();
		String solr = "goodstype2:" + goods.getGoodstype2();
		if (goods.getGoodstype3() != null) {
			solr = "goodstype3:" + goods.getGoodstype3();
		}
		StringBuilder solrQuery = new StringBuilder();
		solrQuery.append(solr);
		while (iterator.hasNext()) {
			String str = iterator.next();
			solrQuery.append(" AND ").append("goodsattributes").append(" : ")
					.append(str);
		}
		query.setQuery(solrQuery.toString());
		List<GoodsVo> beans = queryBeans(query);
		return beans;
	}

	/**
	 * 通过价格区间查询商品.
	 * 
	 * @param goods
	 *            商品对象.
	 * @param minPrice
	 *            最低价格.
	 * @param maxPrice
	 *            最高价格.
	 * @return beans 结果集.
	 * @throws Exception
	 */
	public List<GoodsVo> queryBeans(GoodsVo goods, float minPrice,
			float maxPrice) throws Exception {
		SolrQuery query = new SolrQuery();
		StringBuilder solrQuery = new StringBuilder();
		// 添加搜索价格区间语句
		String price = "goodsprice" + ":" + "[" + String.valueOf(minPrice)
				+ " TO " + String.valueOf(maxPrice) + "]";
		solrQuery.append(price);

		// 添加搜索类别语句
		String type = "goodstype2:" + goods.getGoodstype2();
		if (goods.getGoodstype3() != null) {
			type = "goodstype3:" + goods.getGoodstype3();
		}
		solrQuery.append(" AND ").append(type);

		// 添加搜索属性语句
		Iterator<String> iterator = goods.getGoodsattributes().iterator();
		while (iterator.hasNext()) {
			String str = iterator.next();
			solrQuery.append(" AND ").append("goodsattributes").append(" : ")
					.append(str);
		}
		query.setQuery(solrQuery.toString());
		List<GoodsVo> beans = queryBeans(query);
		return beans;
	}

	/**
	 * 根据关键字查询商品.
	 * @param label
	 * 			关键字.
	 * @return beans
	 * 			结果集.
	 * @throws Exception
	 */
	public List<Object> queryBean(String label)
			throws Exception {
		// 得到查询语句 //OR 连接查询语句
		String queryString = getSpecifyQueryString(label);

		SolrClient queryClient = this.masterFactory.getQuerySolrClient();
		SolrQuery query = new SolrQuery(queryString);

		query.setHighlight(true);

		// 指定属性的高亮
		query.addHighlightField("goodsname");

		query.setHighlightSimplePre("<font color=\"red\">");
		query.setHighlightSimplePost("</font>");

		QueryResponse response = queryClient.query(query);
		SolrDocumentList doucmentList = new SolrDocumentList();
		SolrDocument document = null;

		SolrDocumentList documents = response.getResults();

		// 第一个Map的键是文档的ID，第二个Map的键是高亮显示的字段名
		Map<String, Map<String, List<String>>> map = response.getHighlighting();

		//将高亮后的数据替换原来的数据
		for (int i = 0; i < documents.size(); i++) {
			document = documents.get(i);
			List<String> list = map.get(document.getFieldValue("id")).get(
					"goodsname");
			if (list != null) {
				document.setField("goodsname", list.get(0));
			}
			doucmentList.add(document);
		}
		List<Object> beans = DocumentToBean(doucmentList,
				GoodsVo.class);
		return beans;
	}

	/**
	 * 将Document转换成指定类型对象 ***暂时只用于高亮显示***
	 * 
	 * @param doucmentList
	 *            documentList类型.
	 * @param clazz
	 *            需要转换成的类型.
	 * @return List<Object> 结果集.
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	public List<Object> DocumentToBean(SolrDocumentList doucmentList,
			Class clazz) throws Exception {
		Object object;
		List<Object> list = new ArrayList<Object>();
		for (SolrDocument solrDocument : doucmentList) {
			object = clazz.newInstance();
			BeanMap beanMap = new BeanMap(object);
			Iterator<Object> iterator = beanMap.keySet().iterator();
			while (iterator.hasNext()) {
				String pro = (String) iterator.next();
				// object 固定有的class成员域，除掉
				if ("class".equals(pro)) {
					continue;
				}
				// 得到solrDocument中指定的属性值
				Object value = solrDocument.get(pro);
				if (value != null) {
					// System.out.println(value.getClass());
				}
				Method writeMethod = beanMap.getWriteMethod(pro);
				// 如果value值为空，报空异常
				writeMethod.invoke(object, value);
			}// end while
			list.add(object);
		}
		return list;
	}

	/**
	 * 根据父类型统计商品子类型.
	 * 
	 * @return godosTypes 所有子级类型.
	 * @throws Exception
	 */
	public List<String> getGoodsType(GoodsVo goods) throws Exception {

		SolrClient querySolrClient = this.masterFactory.getQuerySolrClient();
		FacetFilter facetFilter = new FacetFilter();
		SolrQuery solrQuery = new SolrQuery();

		if (goods.getGoodstype1() == null) {
			solrQuery.setQuery("* : *");
			facetFilter.addFacetField("goodstype1");
		} else if (goods.getGoodstype1() != null
				|| goods.getGoodstype2() == null) {
			solrQuery.setQuery("goodstype1:" + goods.getGoodstype1());
			facetFilter.addFacetField("goodstype2");
		} else if (goods.getGoodstype2() != null
				|| goods.getGoodstype3() == null) {
			solrQuery.setQuery("goodstype2:" + goods.getGoodstype1());
			facetFilter.addFacetField("goodstype3");
		}
		QueryBuilder queryBuilder = new QueryBuilder();
		queryBuilder.addFilter(facetFilter);
		SolrQuery build = queryBuilder.build(solrQuery);

		QueryResponse response = querySolrClient.query(build);
		List<FacetField> facetFields = response.getFacetFields();
		List<String> goodsTypes = new ArrayList<String>();
		for (FacetField facetField : facetFields) {
			// System.out.println(facetField.getValues());
			for (Count string : facetField.getValues()) {
				goodsTypes.add(string.getName());
			}
		}
		return goodsTypes;
	}

	/**
	 * 根据最低类别获取商品的属性名跟属性值.
	 * 
	 * @param goods
	 *            商品对象.
	 * @return map 属性名跟属性值
	 * @throws Exception
	 */
	public Map<String, List<String>> getAttributesByType(GoodsVo goods)
			throws Exception {

		SolrClient querySolrClient = this.masterFactory.getQuerySolrClient();
		SolrQuery solrQuery = new SolrQuery();
		FacetFilter facetFilter = new FacetFilter();

		if (goods.getGoodstype3() != null) {
			solrQuery.setQuery("goodstype3:" + goods.getGoodstype3());
		} else {
			solrQuery.setQuery("goodstype2:" + goods.getGoodstype2());
		}
		facetFilter.addFacetField("goodsattributes");

		QueryBuilder queryBuilder = new QueryBuilder();
		queryBuilder.addFilter(facetFilter);
		SolrQuery build = queryBuilder.build(solrQuery);

		QueryResponse response = querySolrClient.query(build);
		List<FacetField> facetFields = response.getFacetFields();
		Map<String, List<String>> map = new HashMap<String, List<String>>();
		for (FacetField facetField : facetFields) {
			// System.out.println(facetField.getValues());
			for (Count string : facetField.getValues()) {
				int pos = string.getName().indexOf("_");
				String attributeName = string.getName().substring(0, pos);
				String attributeValue = string.getName().substring(pos + 1);
				if (map.get(attributeName) != null) {
					List<String> valueList = new ArrayList<String>();
					valueList = map.get(attributeName);
					valueList.add(attributeValue);
					map.put(attributeName, valueList);
				} else {
					List<String> valueList = new ArrayList<String>();
					valueList.add(attributeValue);
					map.put(attributeName, valueList);
				}

			}
		}
		return map;
	}

	/**
	 * 得到对象的非空属性，并且封装成map返回.
	 * 
	 * @param object
	 *            对象.
	 * @return notNullParam 非空属性的Map.
	 */
	private Map<String, Object> getNotNullProperties(Object object) {

		Map<String, Object> notNullParam = null;
		BeanMap beanMap = new BeanMap(object);
		notNullParam = new HashMap<String, Object>();
		Iterator<Object> keyIterator = beanMap.keySet().iterator();
		String propertyName = null;
		while (keyIterator.hasNext()) {
			propertyName = (String) keyIterator.next();

			if (propertyName.equals("class")
					|| beanMap.get(propertyName) == null) {
				continue;
			}
			notNullParam.put(propertyName, beanMap.get(propertyName));
		}
		return notNullParam;
	}

	/**
	 * 关键字查询语句的拼接.
	 * @param label
	 * 			关键字.
	 * @return label
	 * 			查询语句.
	 */
	private String getSpecifyQueryString(String label) {
		StringBuilder querybuilder = new StringBuilder();
		querybuilder.append("goodsname").append(" : ").append(label)
				.append(" OR ").append("goodslabel").append(" : ")
				.append(label);
		String query = querybuilder.toString();
		return query;
	}

	/**
	 * 通过对象非空类型map动态组装成 AND 查询语句.
	 * 
	 * @param notNullProperties
	 *            非空类型集.
	 * @return query AND的查询语句.
	 */
	@SuppressWarnings("unused")
	private String getQueryStringWithAND(Map<String, Object> notNullProperties) {
		StringBuilder querybuilder = new StringBuilder();
		Iterator<String> notNullParams = notNullProperties.keySet().iterator();
		while (notNullParams.hasNext()) {
			String key = notNullParams.next();
			querybuilder.append(key).append(" : ")
					.append(notNullProperties.get(key));
			if (notNullParams.hasNext()) {
				querybuilder.append(" AND ");
			}
		}
		String query = querybuilder.toString();
		return query;
	}

	/**
	 * 通过对象非空类型map动态组装成 OR 查询语句.
	 * 
	 * @param notNullProperties
	 *            非空类型集.
	 * @return query AND的查询语句.
	 */
	private String getQueryStringWithOR(Map<String, Object> notNullProperties) {
		StringBuilder querybuilder = new StringBuilder();
		Iterator<String> notNullParams = notNullProperties.keySet().iterator();
		while (notNullParams.hasNext()) {
			String key = notNullParams.next();
			if (notNullProperties.get(key) instanceof List) {
				querybuilder.append(key).append(" : ")
						.append(notNullProperties.get(key)); // 得到List的第一个试试
			} else {
				querybuilder.append(key).append(" : ")
						.append(notNullProperties.get(key));
			}
			if (notNullParams.hasNext()) {
				querybuilder.append(" OR ");
			}
		}
		String query = querybuilder.toString();
		return query;
	}
}
