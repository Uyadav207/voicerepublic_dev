require 'spec_helper'


describe "Conversations" do
 
  before do
    @user = FactoryGirl.create(:user)
    #@klu = FactoryGirl.create(:published_no_kluuu, :user => @user)
  end
  
  describe "GET /conversations" do
    it "shows a headline mentioning last conversations" do
      login_user(@user)
      visit user_conversations_path(:user_id => @user)
    end
  end
end
