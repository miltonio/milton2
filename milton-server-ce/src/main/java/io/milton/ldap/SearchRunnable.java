/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.milton.ldap;

import io.milton.resource.LdapContact;
import io.milton.common.LogUtils;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.values.ValueAndType;
import io.milton.ldap.LdapPropertyMapper.LdapMappedProp;
import java.io.IOException;
import java.net.SocketException;
import java.util.*;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class SearchRunnable implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(SearchRunnable.class);
	private final UserFactory userFactory;
	private final int currentMessageId;
	private final SearchManager searchManager;
	private final String dn;
	private final int scope;
	private final int sizeLimit;
	private final int timelimit;
	private final LdapFilter ldapFilter;
	private final Set<String> returningAttributes;
	private final LdapPropertyMapper propertyMapper;
	private final Conditions conditions;
	private UUID uuid; // assigned by search manager
	private boolean abandon;
	private final LdapResponseHandler responseHandler;
	private final LdapPrincipal user;

	protected SearchRunnable(UserFactory userFactory, LdapPropertyMapper propertyMapper, int currentMessageId, String dn, int scope, int sizeLimit, int timelimit, LdapFilter ldapFilter, Set<String> returningAttributes, LdapResponseHandler ldapResponseHandler, LdapPrincipal user, SearchManager searchManager) {
		this.userFactory = userFactory;
		this.searchManager = searchManager;
		this.user = user;
		this.responseHandler = ldapResponseHandler;
		this.propertyMapper = propertyMapper;
		this.conditions = new Conditions(propertyMapper);
		this.currentMessageId = currentMessageId;
		this.dn = dn;
		this.scope = scope;
		this.sizeLimit = sizeLimit;
		this.timelimit = timelimit;
		this.ldapFilter = ldapFilter;
		this.returningAttributes = returningAttributes;
	}

	/**
	 * Abandon search.
	 */
	public void abandon() {
		abandon = true;
	}

	@Override
	public void run() {
		try {
			int size = 0;
			LogUtils.debug(log, "LOG_LDAP_REQ_SEARCH", currentMessageId, dn, scope, sizeLimit, timelimit, ldapFilter.toString(), returningAttributes);
			if (scope == Ldap.SCOPE_BASE_OBJECT) {
				log.debug("Check type of search... scope is BASE OBJECT" );
				if ("".equals(dn)) {
					size = 1;
					log.info("send root DSE");
					responseHandler.sendRootDSE(currentMessageId);
				} else if (Ldap.BASE_CONTEXT.equals(dn)) {
					size = 1;
					// root
					// root
					log.info("send base context");
					responseHandler.sendBaseContext(currentMessageId);
				} else if (dn.startsWith("uid=") && dn.indexOf(',') > 0) {
					if (user != null) {
						// single user request
						// single user request
						String uid = dn.substring("uid=".length(), dn.indexOf(','));
						Set<LdapContact> persons = null;
						// first search in contact
						// first search in contact
						try {
							// check if this is a contact uid
							Integer.parseInt(uid);
							persons = contactFind(conditions.isEqualTo("imapUid", uid), returningAttributes, sizeLimit);
						} catch (NumberFormatException e) {
							// ignore, this is not a contact uid
						}
						// then in GAL
						if (persons == null || persons.isEmpty()) {
							List<LdapContact> galContacts = null;
							try {
								log.info("do GAL search: " + uid);
								galContacts = userFactory.galFind(conditions.isEqualTo("imapUid", uid), sizeLimit);
							} catch (NotAuthorizedException ex) {
								log.error("not auth", ex);
							} catch (BadRequestException ex) {
								log.error("bad req", ex);
							}
							if (galContacts != null && galContacts.size() > 0) {
								LdapContact person = galContacts.get(0);
								if (persons == null) {
									persons = new HashSet<LdapContact>();
								}
								persons.add(person);
							}
						}
						size = persons == null ? 0 : persons.size();
						try {
							sendPersons(currentMessageId, dn.substring(dn.indexOf(',')), persons, returningAttributes);
						} catch (NotAuthorizedException ex) {
							log.error("Not authorised", ex);
						} catch (BadRequestException ex) {
							log.error("bad req", ex);
						}
					} else {
						LogUtils.debug(log, "LOG_LDAP_REQ_SEARCH_ANONYMOUS_ACCESS_FORBIDDEN", currentMessageId, dn);
					}
				} else {
					LogUtils.debug(log, "LOG_LDAP_REQ_SEARCH_INVALID_DN (1)", currentMessageId, dn);
				}
			} else if (Ldap.COMPUTER_CONTEXT.equals(dn) || Ldap.COMPUTER_CONTEXT_LION.equals(dn)) {
				size = 1;
				// computer context for iCal
				log.info("send computer context");
				responseHandler.sendComputerContext(currentMessageId, returningAttributes);
			} else if (( dn.equals("")  // Outlook 2010 by default sends no DN
					|| Ldap.BASE_CONTEXT.equalsIgnoreCase(dn)
					|| Ldap.OD_USER_CONTEXT.equalsIgnoreCase(dn))
					|| Ldap.MSLIVE_BASE_CONTEXT.equals(dn)
					|| Ldap.OD_USER_CONTEXT_LION.equalsIgnoreCase(dn)) {
				log.info("not a weird search... check for normal conditions");
				if (user != null) {
					log.debug("we have a user...");
					Set<LdapContact> persons = new HashSet<LdapContact>();
					if (ldapFilter.isFullSearch()) {
						// append personal contacts first
						log.info("do personcal contact search");
						Set<LdapContact> contacts = contactFind(null, returningAttributes, sizeLimit);
						LogUtils.debug(log, "fullSearch: results:", contacts.size());
						for (LdapContact person : contacts) {
							persons.add(person);
							if (persons.size() == sizeLimit) {
								break;
							}
						}

						// full search
						for (char c = 'A'; c <= 'Z'; c++) {
							if (!abandon && persons.size() < sizeLimit) {
								Condition startsWith = conditions.startsWith("cn", String.valueOf(c));
								Collection<LdapContact> galContacts = null;
								try {
									log.info("now do GAL search");
									galContacts = userFactory.galFind(startsWith, sizeLimit);
								} catch (NotAuthorizedException ex) {
									log.error("not auth", ex);
								} catch (BadRequestException ex) {
									log.error("bad req", ex);
								}
								if (galContacts != null) {
									LogUtils.debug(log, "doSearch: results:", contacts.size());
									for (LdapContact person : galContacts) {
										persons.add(person);
										if (persons.size() == sizeLimit) {
											break;
										}
									}
								}
							}
							if (persons.size() == sizeLimit) {
								break;
							}
						}
					} else {
						// append only personal contacts
						log.info("do personcal contact search only");
						Condition filter = ldapFilter.getContactSearchFilter();
						LogUtils.debug(log, "not full search:", filter);
						//if ldapfilter is not a full search and filter is null,
						//ignored all attribute filters => return empty results
						if (ldapFilter.isFullSearch() || filter != null) {
							Set<LdapContact> contacts = contactFind(filter, returningAttributes, sizeLimit);
							for (LdapContact person : contacts) {
								persons.add(person);
								if (persons.size() == sizeLimit) {
									log.debug("EXceeded size limit1");
									break;
								}
							}
							LogUtils.trace(log, "local contacts result size: ", persons.size());
							if (!abandon && persons.size() < sizeLimit) {
								List<LdapContact> galContacts = null;
								try {
									galContacts = ldapFilter.findInGAL(user, returningAttributes, sizeLimit - persons.size());
								} catch (NotAuthorizedException ex) {
									log.error("not auth", ex);
								} catch (BadRequestException ex) {
									log.error("bad req", ex);
								}
								if (galContacts != null) {
									LogUtils.trace(log, "gal contacts result size: ", galContacts.size());
									for (LdapContact person : galContacts) {
										if (persons.size() >= sizeLimit) {
											log.debug("EXceeded size limit2");
											break;
										}
										LogUtils.trace(log, "add contact to results: ", person.getName());
										persons.add(person);
									}
								}
							}
						}
					}
					LogUtils.debug(log, "LOG_LDAP_REQ_SEARCH_FOUND_RESULTS", currentMessageId, persons.size());
					try {
						sendPersons(currentMessageId, ", " + dn, persons, returningAttributes);
					} catch (NotAuthorizedException ex) {
						log.error("not auth", ex);
					} catch (BadRequestException ex) {
						log.error("bad req", ex);
					}
					LogUtils.debug(log, "LOG_LDAP_REQ_SEARCH_END", currentMessageId);
				} else {
					LogUtils.debug(log, "LOG_LDAP_REQ_SEARCH_ANONYMOUS_ACCESS_FORBIDDEN", currentMessageId, dn);
				}
			} else if (dn != null && dn.length() > 0 && !Ldap.OD_CONFIG_CONTEXT.equals(dn) && !Ldap.OD_GROUP_CONTEXT.equals(dn)) {
				LogUtils.debug(log, "LOG_LDAP_REQ_SEARCH_INVALID_DN (2)", currentMessageId, dn);
				log.debug("DN is not equal to: " + Ldap.OD_CONFIG_CONTEXT + " or " + Ldap.OD_GROUP_CONTEXT + " or any other valid pattern. Is: " + dn);
			} else {
				log.warn("Search criteria didnt match any of the expected patterns. Perhaps the user name is missing a context? DN=" + dn + ", expected something like: " + Ldap.OD_USER_CONTEXT);
			}
			// iCal: do not send LDAP_SIZE_LIMIT_EXCEEDED on apple-computer search by cn with sizelimit 1
			if (size > 1 && size == sizeLimit) {
				LogUtils.debug(log, "LOG_LDAP_REQ_SEARCH_SIZE_LIMIT_EXCEEDED", currentMessageId);
				responseHandler.sendClient(currentMessageId, Ldap.LDAP_REP_RESULT, Ldap.LDAP_SIZE_LIMIT_EXCEEDED, "");
			} else {
				log.debug("No search results");
				LogUtils.debug(log, "LOG_LDAP_REQ_SEARCH_SUCCESS", currentMessageId);
				responseHandler.sendClient(currentMessageId, Ldap.LDAP_REP_RESULT, Ldap.LDAP_SUCCESS, "");
			}
		} catch (SocketException e) {
			log.warn("closed connection", e);
		} catch (IOException e) {
			log.error("", e);
			try {
				responseHandler.sendErr(currentMessageId, Ldap.LDAP_REP_RESULT, e);
			} catch (IOException e2) {
				LogUtils.debug(log, "LOG_EXCEPTION_SENDING_ERROR_TO_CLIENT", e2);
			}
		} finally {
			log.debug("search complete");
			searchManager.searchComplete(uuid, currentMessageId);
		}
	}

	/**
	 * Search users in contacts folder
	 *
	 * @param condition search filter
	 * @param returningAttributes requested attributes
	 * @param maxCount maximum item count
	 * @return List of users
	 * @throws IOException on error
	 */
	public Set<LdapContact> contactFind(Condition condition, Set<String> returningAttributes, int maxCount) throws IOException {
		Set<LdapContact> results = new HashSet<LdapContact>();
		List<LdapContact> contacts = user.searchContacts(condition, maxCount);
		LogUtils.trace(log, "contactFind: contacts size:", contacts.size());
		for (LdapContact contact : contacts) {
			results.add(contact);
		}
		return results;
	}

	private void sendPersons(int currentMessageId, String baseContext, Set<LdapContact> persons, Set<String> returningAttributes) throws IOException, NotAuthorizedException, BadRequestException {
		LogUtils.debug(log, "sendPersons", baseContext, "size:", persons.size());
		boolean needObjectClasses = returningAttributes.contains("objectclass") || returningAttributes.isEmpty();
		boolean returnAllAttributes = returningAttributes.isEmpty();
		if (persons.isEmpty()) {
			log.warn("No contacts to send! -------------------");
		}
		for (LdapContact person : persons) {
			if (abandon) {
				log.warn("Abandon flag is set, so exiting send!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
				break;
			}
			Map<String, Object> response = new HashMap<String, Object>();
			Set<LdapMappedProp> props = propertyMapper.mapProperties(returnAllAttributes, returningAttributes, person);

			response.put("uid", person.getName());
			for (LdapMappedProp prop : props) {
				ValueAndType vt;
				try {
					vt = propertyMapper.getProperty(prop.mappedName, person);
				} catch (NotAuthorizedException ex) {
					vt = null;
				}
				if (vt == null) {
					LogUtils.trace(log, "sendPersons: property not found: ldap property: ", prop.ldapName, " - dav prop: ", prop.mappedName, "resource: ", person.getClass());
				} else {
					if (vt.getValue() != null) {
						response.put(prop.ldapName, vt.getValue());
					}
				}
			}

			// Process all attributes which have static mappings
			for (Map.Entry<String, String> entry : Ldap.STATIC_ATTRIBUTE_MAP.entrySet()) {
				String ldapAttribute = entry.getKey();
				String value = entry.getValue();
				if (value != null && (returnAllAttributes || returningAttributes.contains(ldapAttribute))) {
					response.put(ldapAttribute, value);
				}
			}
			if (needObjectClasses) {
				response.put("objectClass", Ldap.PERSON_OBJECT_CLASSES);
			}
			// iCal: copy email to apple-generateduid, encode @
			if (returnAllAttributes || returningAttributes.contains("apple-generateduid")) {
				String mail = (String) response.get("mail");
				if (mail != null) {
					response.put("apple-generateduid", mail.replaceAll("@", "__AT__"));
				} else {
					// failover, should not happen
					// failover, should not happen
					response.put("apple-generateduid", response.get("uid"));
				}
			}
			// iCal: replace current user alias with login name
			if (user.getName().equals(response.get("uid"))) {
				if (returningAttributes.contains("uidnumber")) {
					response.put("uidnumber", user.getName());
				}
			}
			LogUtils.debug(log, "LOG_LDAP_REQ_SEARCH_SEND_PERSON", currentMessageId, response.get("uid"), baseContext, response);
			responseHandler.sendEntry(currentMessageId, "uid=" + response.get("uid") + baseContext, response);
		}
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}
}
