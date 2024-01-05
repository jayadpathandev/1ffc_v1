-- Update the superAdmin user to meet 1st Franklins security requirements.

update auth_users set
	username = 'superAdmin1',
	password = 'b8948fd4772f4ee5af2f437196776bd64b9a4ea4cf995d929d069abb06a500551c72abe516ac3273'
where userid = 1;
