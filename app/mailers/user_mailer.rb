class UserMailer < ActionMailer::Base
  default from: "service@voicerepublic.com"

  def friend_notification(notification)
    @notification = notification
    @user = @notification.user
    I18n.locale = @user.account.preferred_locale
    mail(:to => @user.email, :subject => t('mailers.your_friend_took_action'))
  end

  def content_notification(notification)
    @notification = notification
    @user = @notification.user
    I18n.locale = @user.account.preferred_locale
    mail(:to => @user.email, :subject => t('mailers.activity_around_you'))
  end

  def new_talk_notification(event, user)
    @event = event
    @user = user

    I18n.with_locale locale(user) do
      mail(:to => user.email, :subject => "New Talk")
    end
  end

  def reminder_notification(event, user)
    @event = event
    @user = user

    I18n.with_locale locale(user) do
      mail(:to => user.email, :subject => "Talk Reminder")
    end
  end

  def new_article_notification(article, user)
    @article = article
    @user = user

    I18n.with_locale locale(@user) do
      mail(:to => @user.email, :subject => "New Article")
    end
  end

  def new_comment_notification(comment, user)
    @comment = comment
    @user = user

    I18n.with_locale locale(@user) do
      mail(:to => @user.email, :subject => "New Comment")
    end
  end

  private

  def locale(user)
    user.account.preferred_locale.to_sym
  end
end
