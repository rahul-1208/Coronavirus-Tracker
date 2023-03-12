package com.example.CoronavirusTracker.services;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.*;
import java.util.*;

import javax.annotation.PostConstruct;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.CoronavirusTracker.models.LocationStats;

@Service
public class CoronaVirusDataService {

	private static String COVID_CASES_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv";

	List<LocationStats> allStats = new ArrayList<>();
		
	public List<LocationStats> getAllStats() {
		return allStats;
	}



	@PostConstruct
	@Scheduled(cron="* * 1 * * *")
	public void fetchCasesData() throws IOException, InterruptedException
	{
		
		List<LocationStats> newStats = new ArrayList<>();
		HttpClient httpClient = HttpClient.newHttpClient();
		HttpRequest httpRequest = HttpRequest.newBuilder().uri(URI.create(COVID_CASES_URL)).build();		
		HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
		
		System.out.println(httpResponse.body());
		
		StringReader reader = new StringReader(httpResponse.body());
		Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader);
		for (CSVRecord record : records) {
			LocationStats locationStat = new LocationStats();
			locationStat.setState(record.get("Province/State"));
		    locationStat.setCountry(record.get("Country/Region"));
		    int latestCases = Integer.parseInt(record.get(record.size()-1)) ;
		    int previousDayCases = Integer.parseInt(record.get(record.size()-2));
		    locationStat.setLatestTotalCases(latestCases);
		    locationStat.setDiffFromPrevDay(latestCases - previousDayCases);
		    newStats.add(locationStat);
		}
		this.allStats = newStats ;
		
	}
	
	
}
