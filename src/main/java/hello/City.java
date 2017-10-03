package hello;

public class City {
	private String city;
	private Double distance;
	private String country;
	private Integer id;
	private Double lat;
	private String localizedCountryName;
	private Double lon;
	private Integer memberCount;
	private String zip;
	
	public double distanceTo(double lat, double lon) {
		return 42.0;  // TODO calculate
	}
	
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public Double getDistance() {
		return distance;
	}
	public void setDistance(Double distance) {
		this.distance = distance;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Double getLat() {
		return lat;
	}
	public void setLat(Double lat) {
		this.lat = lat;
	}
	public String getLocalizedCountryName() {
		return localizedCountryName;
	}
	public void setLocalizedCountryName(String localizedCountryName) {
		this.localizedCountryName = localizedCountryName;
	}
	public Double getLon() {
		return lon;
	}
	public void setLon(Double lon) {
		this.lon = lon;
	}
	public Integer getMemberCount() {
		return memberCount;
	}
	public void setMemberCount(Integer memberCount) {
		this.memberCount = memberCount;
	}
	public String getZip() {
		return zip;
	}
	public void setZip(String zip) {
		this.zip = zip;
	}
}
