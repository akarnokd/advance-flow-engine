/*
 * Copyright 2010-2013 The Advance EU 7th Framework project consortium
 *
 * This file is part of Advance.
 *
 * Advance is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * Advance is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Advance.  If not, see
 * <http://www.gnu.org/licenses/>.
 *
 */

package eu.advance.logistics.live.reporter.ws;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.annotation.Resource;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.ws.WebServiceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.advance.logistics.live.reporter.db.UserDB;
import eu.advance.logistics.live.reporter.model.User;

//download RI fromhttps://jax-ws.java.net/
//copy jars from lib into tomcat/lib

//Based on article:
//http://www.mkyong.com/webservices/jax-ws/deploy-jax-ws-web-services-on-tomcat/

/**
 * 
 */
@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC)
public class ImportSEI {
	/** The logger.  */
	protected static final Logger LOGGER = LoggerFactory.getLogger(ImportSEI.class);
	/** Context. */
	@Resource
	WebServiceContext wsctx;	
	/**
	 * The entry point method.
	 * @param datapack the import data
	 * @return the response 
	 */
	@WebMethod
	public String importData(DataPack datapack) {
		LOGGER.info("Import start");
		if (datapack.userName == null || datapack.userName.isEmpty()
				|| datapack.password == null || datapack.password.isEmpty()) {
			LOGGER.error("Missing username or password");
			return "Missing username or password.";
		}
		User u = UserDB.getUser(datapack.userName);
		if (u == null || !u.verify(datapack.password)) {
			LOGGER.error("Invalid username or password");
			return "Invalid username or password.";
		}

		return "OK";
	}

	/**
	 * Save the datapack into an XML file.
	 * @param dp the datapack
	 * @throws JAXBException on error
	 */
	public static void saveToFile(DataPack dp) throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(DataPack.class);
		Marshaller m = context.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
		m.marshal(dp, new File("imported_" + sdf.format(new Date()) + ".xml"));
		LOGGER.debug(new File("imported_" + sdf.format(new Date()) + ".xml").getAbsolutePath());
	}
	/**
	 * Loads a datapack from an XML file.
	 * @param file the XML file
	 * @return the parsed datapack
	 * @throws JAXBException on error
	 * @throws FileNotFoundException on error
	 */
	public static  DataPack loadFromFile(File file) throws JAXBException, FileNotFoundException {
		JAXBContext context = JAXBContext.newInstance(DataPack.class);
		Unmarshaller um = context.createUnmarshaller();
		DataPack dp = (DataPack) um.unmarshal(new FileReader(file));
		return dp;
	}
}
