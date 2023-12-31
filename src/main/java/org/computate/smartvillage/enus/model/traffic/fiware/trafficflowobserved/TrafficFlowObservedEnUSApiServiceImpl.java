/*
 * Copyright (c) 2018-2022 Computate Limited Liability Company in Utah, USA,
 * and the Smarta Byar Smart Village FIWARE IHub. 
 *
 * This program and the accompanying materials are made available under the
 * terms of the GNU GENERAL PUBLIC LICENSE Version 3 which is available at
 * 
 * https://www.gnu.org/licenses/gpl-3.0.en.html
 * 
 * You may not propagate or modify a covered work except as expressly provided 
 * under this License. Any attempt otherwise to propagate or modify it is void, 
 * and will automatically terminate your rights under this License (including 
 * any patent licenses granted under the third paragraph of section 11).
 */
package org.computate.smartvillage.enus.model.traffic.fiware.trafficflowobserved;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.computate.search.tool.SearchTool;
import org.computate.smartvillage.enus.model.traffic.fiware.smarttrafficlight.SmartTrafficLight;
import org.computate.smartvillage.enus.model.traffic.simulation.report.SimulationReport;
import org.computate.smartvillage.enus.request.SiteRequestEnUS;
import org.computate.vertx.search.list.SearchList;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.authorization.AuthorizationProvider;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.templ.handlebars.HandlebarsTemplateEngine;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.pgclient.PgPool;

/**
 * Translate: false
 **/
public class TrafficFlowObservedEnUSApiServiceImpl extends TrafficFlowObservedEnUSGenApiServiceImpl {

	public TrafficFlowObservedEnUSApiServiceImpl(EventBus eventBus, JsonObject config, WorkerExecutor workerExecutor, PgPool pgPool, KafkaProducer<String, String> kafkaProducer, WebClient webClient, OAuth2Auth oauth2AuthenticationProvider, AuthorizationProvider authorizationProvider, HandlebarsTemplateEngine templateEngine) {
		super(eventBus, config, workerExecutor, pgPool, kafkaProducer, webClient, oauth2AuthenticationProvider, authorizationProvider, templateEngine);
	}

	public Future<Void> refreshTrafficFlowObserved(TrafficFlowObserved o) {
		Promise<Void> promise = Promise.promise();
		SiteRequestEnUS siteRequest = o.getSiteRequest_();
		try {
			Boolean refresh = !"false".equals(siteRequest.getRequestVars().get("refresh"));
			if(refresh 
					&& !Optional.ofNullable(siteRequest.getJsonObject()).map(JsonObject::isEmpty).orElse(true)
					&& o.getCustomTrafficLightId() != null
					) {

				SearchList<SimulationReport> searchReportList2 = new SearchList<SimulationReport>();
				searchReportList2.setStore(true);
				searchReportList2.q("*:*");
				searchReportList2.setC(SimulationReport.class);
				searchReportList2.fq(SimulationReport.VAR_smartTrafficLightId + "_docvalues_string:" + SearchTool.escapeQueryChars(o.getCustomTrafficLightId()));
				searchReportList2.rows(50L);
				searchReportList2.promiseDeepSearchList(siteRequest).onSuccess(b -> {
	
					if(searchReportList2.size() > 0) {
						String id2 = searchReportList2.first().getSmartTrafficLightId();
						String classSimpleName2 = SmartTrafficLight.CLASS_SIMPLE_NAME;
	
						if("SmartTrafficLight".equals(classSimpleName2) && id2 != null) {
							SearchList<SmartTrafficLight> searchList2 = new SearchList<SmartTrafficLight>();
							searchList2.setStore(true);
							searchList2.q("*:*");
							searchList2.setC(SmartTrafficLight.class);
							searchList2.fq(SmartTrafficLight.VAR_entityId + "_docvalues_string:" + SearchTool.escapeQueryChars(id2));
							searchList2.rows(1L);
							searchList2.promiseDeepSearchList(siteRequest).onSuccess(c -> {
								SmartTrafficLight o2 = searchList2.getList().stream().findFirst().orElse(null);
								if(o2 != null) {
									JsonObject params = new JsonObject();
									params.put("body", new JsonObject());
									params.put("cookie", new JsonObject());
									params.put("path", new JsonObject());
									params.put("query", new JsonObject().put("q", "*:*").put("fq", new JsonArray().add(SmartTrafficLight.VAR_entityId + ":" + id2)).put("var", new JsonArray().add("refresh:false")));
									JsonObject context = new JsonObject().put("params", params).put("user", siteRequest.getUserPrincipal());
									JsonObject json = new JsonObject().put("context", context);
									LOG.info("o2.getParamMinGreenTimeSecSouthNorth(): " + o2.getParamMinGreenTimeSecSouthNorth());
									eventBus.request("smartabyar-smartvillage-enUS-SmartTrafficLight", json, new DeliveryOptions().addHeader("action", "patchSmartTrafficLightFuture")).onSuccess(d -> {
										JsonObject responseMessage = (JsonObject)d.body();
										Integer statusCode = responseMessage.getInteger("statusCode");
										if(statusCode.equals(200)) {
											List<Future> futures = new ArrayList<>();
											for(SimulationReport o3 : searchReportList2.getList()) {
												Long pk2 = o3.getPk();
												futures.add(Future.future(promise2 -> {
													JsonObject params3 = new JsonObject();
													params3.put("body", new JsonObject());
													params3.put("cookie", new JsonObject());
													params3.put("path", new JsonObject());
													params3.put("query", new JsonObject().put("q", "*:*").put("fq", new JsonArray().add("pk:" + pk2)).put("var", new JsonArray().add("refresh:false")));
													JsonObject context3 = new JsonObject().put("params", params3).put("user", siteRequest.getUserPrincipal());
													JsonObject json3 = new JsonObject().put("context", context3);
													eventBus.request("smartabyar-smartvillage-enUS-SimulationReport", json3, new DeliveryOptions().addHeader("action", "patchSimulationReportFuture")).onSuccess(e -> {
														JsonObject responseMessage3 = (JsonObject)e.body();
														Integer statusCode3 = responseMessage3.getInteger("statusCode");
														if(statusCode3.equals(200))
															promise2.complete();
														else
															promise2.fail(new RuntimeException(responseMessage3.getString("statusMessage")));
													}).onFailure(ex -> {
														promise2.fail(ex);
													});
												}));
											}
	
											CompositeFuture.all(futures).onSuccess(e -> {
												JsonObject params3 = new JsonObject();
												params3.put("body", new JsonObject());
												params3.put("cookie", new JsonObject());
												params3.put("header", new JsonObject());
												params3.put("form", new JsonObject());
												params3.put("path", new JsonObject());
												JsonObject query = new JsonObject();
												Boolean softCommit = Optional.ofNullable(siteRequest.getServiceRequest().getParams()).map(p -> p.getJsonObject("query")).map( q -> q.getBoolean("softCommit")).orElse(null);
												Integer commitWithin = Optional.ofNullable(siteRequest.getServiceRequest().getParams()).map(p -> p.getJsonObject("query")).map( q -> q.getInteger("commitWithin")).orElse(null);
												if(softCommit == null && commitWithin == null)
													softCommit = true;
												if(softCommit != null)
													query.put("softCommit", softCommit);
												if(commitWithin != null)
													query.put("commitWithin", commitWithin);
												query.put("q", "*:*").put("fq", new JsonArray().add("id:" + o.getId())).put("var", new JsonArray().add("refresh:false"));
												params3.put("query", query);
												JsonObject context3 = new JsonObject().put("params", params3).put("user", siteRequest.getUserPrincipal());
												JsonObject json3 = new JsonObject().put("context", context3);
												eventBus.request("smartabyar-smartvillage-enUS-TrafficFlowObserved", json3, new DeliveryOptions().addHeader("action", "patchTrafficFlowObservedFuture")).onSuccess(f -> {
													JsonObject responseMessage3 = (JsonObject)f.body();
													Integer statusCode3 = responseMessage3.getInteger("statusCode");
													if(statusCode3.equals(200))
														promise.complete();
													else
														promise.fail(new RuntimeException(responseMessage3.getString("statusMessage")));
												}).onFailure(ex -> {
													LOG.error("Refresh relations failed. ", ex);
													promise.fail(ex);
												});
											}).onFailure(ex -> {
												LOG.error("Refresh relations failed. ", ex);
												promise.fail(ex);
											});
										} else {
											promise.fail(new RuntimeException(responseMessage.getString("statusMessage")));
										}
									}).onFailure(ex -> {
										promise.fail(ex);
									});
								} else {
									promise.complete();
								}
							}).onFailure(ex -> {
								promise.fail(ex);
							});
						} else {
							promise.complete();
						}
					} else {
						promise.complete();
					}
				}).onFailure(ex -> {
					promise.fail(ex);
				});
			} else {
				promise.complete();
			}
		} catch(Exception ex) {
			LOG.error(String.format("refreshTrafficFlowObserved failed. "), ex);
			promise.fail(ex);
		}
		return promise.future();
	}
}
