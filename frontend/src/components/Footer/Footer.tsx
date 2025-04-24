import { IconInstagram, IconFacebook, IconTelegram, IconTwitter, IconYoutube, IconVk } from "../../utils/icons"
import "./Footer.css"

export default function Footer() {
    let socials = [
        {
            text: "Instagram",
            icon: IconInstagram,
            url: "https://google.com"
        },
        {
            text: "Facebook",
            icon: IconFacebook,
            url: "https://google.com"
        },
        {
            text: "Telegram",
            icon: IconTelegram,
            url: "https://google.com"
        },
        {
            text: "Twitter",
            icon: IconTwitter,
            url: "https://google.com"
        },
        {
            text: "Youtube",
            icon: IconYoutube,
            url: "https://google.com"
        },
        {
            text: "VK",
            icon: IconVk,
            url: "https://google.com"
        },
    ];

    return (
        <footer className="Footer">
            <p className="Footer_SubText">2025 @ Raise | Best book shop in the universe</p>
            <p className="Footer_PreSocialText">Ссылки на наши соц сети:</p>
            <div className="Footer_Socials">
                {
                    socials.map((socialItem, index) => {
                        return (
                            <div className="Footer_SocialBlock" key={ index }>
                                <socialItem.icon className="Footer_SocialBlock_icon"/>
                                <a href={ socialItem.url } target="_blank" className="Footer_SocialBlock_Ref">{socialItem.text}</a>
                            </div>
                        )  
                    })
                }
            </div>
        </footer>
    )

}